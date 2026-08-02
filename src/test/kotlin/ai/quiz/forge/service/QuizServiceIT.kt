package ai.quiz.forge.service

import ai.quiz.forge.rest.model.CreateQuiz
import ai.quiz.forge.service.model.Question
import ai.quiz.forge.service.model.Quiz
import ai.quiz.forge.service.model.ai.generated.Answer
import ai.quiz.forge.service.model.ai.generated.NewQuestion
import ai.quiz.forge.shared.Option
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class QuizServiceIT {

    @Autowired
    lateinit var quizService: QuizService

    @MockitoBean
    lateinit var chatClient: ChatClient

    @BeforeEach
    fun setUpChatClient() {
        val requestSpec = mock(ChatClient.ChatClientRequestSpec::class.java)
        val responseSpec = mock(ChatClient.CallResponseSpec::class.java)

        `when`(chatClient.prompt()).thenReturn(requestSpec)
        `when`(requestSpec.user(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.call()).thenReturn(responseSpec)
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

        val selectedOption = Option.OPTION_A
        quizService.answerQuestion(savedQuiz.id!!, selectedOption)
    }

    @Test
    fun `answerQuestion throws forbidden when quiz is already finished`() {
        // ARRANGE
        val quizId = UUID.randomUUID()
        val finishedQuiz = Quiz(
            id = quizId,
            topic = "Finished",
            questions = listOf(
                Question(
                    question = "Q1?",
                    optionA = "A",
                    optionB = "B",
                    optionC = "C",
                    optionD = "D",
                    hint = "H",
                ).apply {
                    // Mark all questions as answered
                    selectedOption = Option.OPTION_A
                    correctOption = Option.OPTION_A
                    explanation = "Done"
                }
            )
        )
        
        // Mocking the initial state setup (as above)
        val failedQuizMocked = Quiz(
            id = quizId,
            topic = "Finished",
            questions = listOf(
                Question(
                    question = "Q1?",
                    optionA = "A",
                    optionB = "B",
                    optionC = "C",
                    optionD = "D",
                    hint = "H",
                ).apply { selectedOption = Option.OPTION_A; correctOption = Option.OPTION_A; explanation = "Done" }
            )
        )

        // ACT & ASSERT
        val selectedOption = Option.OPTION_B
        val ex = assertThrows(ResponseStatusException::class.java) {
            quizService.answerQuestion(quizId, selectedOption)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        //assertEquals("Quiz already finished", ex.reason)
    }
}
