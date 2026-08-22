package ai.quiz.forge.service

import ai.quiz.forge.rest.model.CreateQuiz
import ai.quiz.forge.persistence.repository.QuizRepository
import ai.quiz.forge.service.model.ai.generated.Answer
import ai.quiz.forge.service.model.ai.generated.NewQuestion
import ai.quiz.forge.shared.Option
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.ChatClientAttributes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import java.util.function.Consumer

@SpringBootTest
@ActiveProfiles("test")
class QuizServiceIT {

    private companion object {
        private const val DEFAULT_QUESTION_DRAFT = """
            Question: What is the largest land animal?
            A) Elephant
            B) Lion
            C) Giraffe
            D) Horse
            Hint: It has a trunk.
        """
    }

    @Autowired
    lateinit var quizService: QuizService

    @Autowired
    lateinit var quizRepository: QuizRepository

    @MockitoBean
    lateinit var chatClient: ChatClient

    private lateinit var requestSpec: ChatClient.ChatClientRequestSpec
    private lateinit var responseSpec: ChatClient.CallResponseSpec
    private lateinit var advisorSpec: ChatClient.AdvisorSpec

    @BeforeEach
    fun setUpChatClient() {
        requestSpec = mock(ChatClient.ChatClientRequestSpec::class.java)
        responseSpec = mock(ChatClient.CallResponseSpec::class.java)
        advisorSpec = mock(ChatClient.AdvisorSpec::class.java)

        `when`(chatClient.prompt()).thenReturn(requestSpec)
        `when`(requestSpec.advisors(any<Consumer<ChatClient.AdvisorSpec>>())).thenAnswer { invocation ->
            invocation.getArgument<Consumer<ChatClient.AdvisorSpec>>(0).accept(advisorSpec)
            requestSpec
        }
        `when`(requestSpec.user(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.call()).thenReturn(responseSpec)
        `when`(responseSpec.content()).thenReturn(DEFAULT_QUESTION_DRAFT)
        `when`(responseSpec.entity(NewQuestion::class.java)).thenReturn(
            NewQuestion(
                question = "What is the largest land animal?",
                optionA = "Elephant",
                optionB = "Lion",
                optionC = "Giraffe",
                optionD = "Horse",
                hint = "It has a trunk.",
            )
        )
        `when`(responseSpec.entity(Answer::class.java)).thenReturn(
            Answer(
                correctOption = Option.OPTION_A,
                explanation = "An elephant is the largest land animal.",
            )
        )
    }

    @Test
    fun `createQuiz generates draft content then structures it with native output`() {
        val questionDraft = "Draft question that must be preserved exactly.\n  Keep this indentation."
        `when`(responseSpec.content()).thenReturn(questionDraft)

        val createdQuiz = quizService.createQuiz(
            CreateQuiz(
                topic = "Animals",
                numberOfQuestions = CreateQuiz.NumberOfQuestions.FIVE,
                difficulty = CreateQuiz.Difficulty.BEGINNER,
            )
        )

        verify(responseSpec, times(5)).content()
        verify(responseSpec, times(5)).entity(NewQuestion::class.java)
        verify(chatClient, times(10)).prompt()
        verify(requestSpec, times(10)).call()
        verify(advisorSpec, times(5)).param(ChatClientAttributes.STRUCTURED_OUTPUT_NATIVE.key, false)

        val promptCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(requestSpec, times(10)).user(promptCaptor.capture())
        val generationPrompt = promptCaptor.allValues[0]
        val structuringPrompt = promptCaptor.allValues[1]
        assertTrue(generationPrompt.contains("Create a single quiz question about the topic \"Animals\""))
        assertTrue(structuringPrompt.contains("Convert the quiz draft below into the native NewQuestion schema fields"))
        assertTrue(structuringPrompt.contains("<quiz-question-draft>\n$questionDraft\n</quiz-question-draft>"))
        assertEquals("What is the largest land animal?", createdQuiz.questions.first().question)
        assertEquals("Elephant", createdQuiz.questions.first().optionA)
    }

    @Test
    fun `createQuiz retries when draft is blank and skips structuring for that failed attempt`() {
        `when`(responseSpec.content()).thenReturn("   ", DEFAULT_QUESTION_DRAFT)

        quizService.createQuiz(
            CreateQuiz(
                topic = "Animals",
                numberOfQuestions = CreateQuiz.NumberOfQuestions.FIVE,
                difficulty = CreateQuiz.Difficulty.BEGINNER,
            )
        )

        verify(responseSpec, times(6)).content()
        verify(responseSpec, times(5)).entity(NewQuestion::class.java)
    }

    @Test
    fun `createQuiz retries when draft is null and skips structuring for that failed attempt`() {
        `when`(responseSpec.content()).thenReturn(null, DEFAULT_QUESTION_DRAFT)

        quizService.createQuiz(
            CreateQuiz(
                topic = "Animals",
                numberOfQuestions = CreateQuiz.NumberOfQuestions.FIVE,
                difficulty = CreateQuiz.Difficulty.BEGINNER,
            )
        )

        verify(responseSpec, times(6)).content()
        verify(responseSpec, times(5)).entity(NewQuestion::class.java)
    }

    @Test
    fun `createQuiz retries whole pair when structuring fails`() {
        `when`(responseSpec.entity(NewQuestion::class.java))
            .thenThrow(RuntimeException("structuring failed"))
            .thenReturn(
                NewQuestion(
                    question = "What is the largest land animal?",
                    optionA = "Elephant",
                    optionB = "Lion",
                    optionC = "Giraffe",
                    optionD = "Horse",
                    hint = "It has a trunk.",
                )
            )

        quizService.createQuiz(
            CreateQuiz(
                topic = "Animals",
                numberOfQuestions = CreateQuiz.NumberOfQuestions.FIVE,
                difficulty = CreateQuiz.Difficulty.BEGINNER,
            )
        )

        verify(responseSpec, times(6)).content()
        verify(responseSpec, times(6)).entity(NewQuestion::class.java)
    }

    @Test
    fun `createQuiz fails after five paired attempts and does not persist partial quiz`() {
        val quizCountBefore = quizRepository.count()
        `when`(responseSpec.entity(NewQuestion::class.java))
            .thenThrow(RuntimeException("structuring failed"))

        val ex = assertThrows(RuntimeException::class.java) {
            quizService.createQuiz(
                CreateQuiz(
                    topic = "Animals",
                    numberOfQuestions = CreateQuiz.NumberOfQuestions.FIVE,
                    difficulty = CreateQuiz.Difficulty.BEGINNER,
                )
            )
        }

        assertEquals("Failed to generate question #1 of 5 after 5 attempts", ex.message)
        assertEquals(quizCountBefore, quizRepository.count())
        verify(responseSpec, times(5)).content()
        verify(responseSpec, times(5)).entity(NewQuestion::class.java)
    }

    @Test
    fun `getQuiz throws not found for unknown id`() {
        val unknownId = UUID.randomUUID()

        val ex = assertThrows(ResponseStatusException::class.java) {
            quizService.getQuiz(unknownId)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        assertEquals("Quiz with id $unknownId not found", ex.reason)
    }

    @Test
    fun `answerQuestion successfully answers the current question and saves state`() {
        val savedQuiz = quizService.createQuiz(
            CreateQuiz(
                topic = "Animals",
                numberOfQuestions = CreateQuiz.NumberOfQuestions.FIVE,
                difficulty = CreateQuiz.Difficulty.BEGINNER
            )
        )

        val quizId = requireNotNull(savedQuiz.id)
        val questionIdsBefore = requireNotNull(quizRepository.findWithQuestionsById(quizId))
            .questions
            .map { it.id }

        val selectedOption = Option.OPTION_A
        quizService.answerQuestion(quizId, selectedOption)

        val questionIdsAfter = requireNotNull(quizRepository.findWithQuestionsById(quizId))
            .questions
            .map { it.id }
        assertEquals(questionIdsBefore, questionIdsAfter)
    }

    @Test
    fun `answerQuestion throws forbidden when quiz is already finished`() {
        val savedQuiz = quizService.createQuiz(
            CreateQuiz(
                topic = "Animals",
                numberOfQuestions = CreateQuiz.NumberOfQuestions.FIVE,
                difficulty = CreateQuiz.Difficulty.BEGINNER,
            )
        )

        val quizId = requireNotNull(savedQuiz.id)
        repeat(savedQuiz.questions.size) {
            quizService.answerQuestion(quizId, Option.OPTION_A)
        }

        val ex = assertThrows(ResponseStatusException::class.java) {
            quizService.answerQuestion(quizId, Option.OPTION_B)
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
        assertEquals("Quiz already finished", ex.reason)
    }
}
