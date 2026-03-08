package ai.quiz.forge.service

import ai.quiz.forge.persistence.repository.QuizRepository
import ai.quiz.forge.rest.model.CreateQuiz
import ai.quiz.forge.service.mapper.NewQuizToQuizMapper
import ai.quiz.forge.service.mapper.QuizEntityToQuizMapper
import ai.quiz.forge.service.mapper.QuizToQuizEntityMapper
import ai.quiz.forge.service.model.Quiz
import ai.quiz.forge.service.model.ai.generated.Answer
import ai.quiz.forge.service.model.ai.generated.NewQuiz
import ai.quiz.forge.shared.Option
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.entity
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class QuizService(
    private val quizRepository: QuizRepository,
    private val chatClient: ChatClient,
) {

    fun createQuiz(createQuiz: CreateQuiz): Quiz {
        val newQuiz = chatClient.prompt().user(
            """
            Create a Quiz about the topic "${createQuiz.topic}".
            The quiz should have ${createQuiz.numberOfQuestions.toString().lowercase()} questions
            and be of ${createQuiz.difficulty.toString().lowercase()} difficulty.
            The questions should have exactly 4 options and only one correct option.
            The Hint should have very specific Information and should not repeat the information given from the question itself.
            """.trimIndent()
        ).call().entity<NewQuiz>()

        return NewQuizToQuizMapper(createQuiz.topic, newQuiz)
            .run(::save)
    }

    @Transactional(readOnly = true)
    fun getQuiz(id: UUID): Quiz =
        id.run(quizRepository::findById)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz with id $id not found")
            }
            .run(QuizEntityToQuizMapper)

    fun answerQuestion(quizId: UUID, selectedOption: Option): Answer {
        val quiz = getQuiz(quizId)

        /**
         * It seems like the AI should answer each question separately to archive better results.
         **/
        val currentQuestion = quiz.questions.find { it.selectedOption == null } ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Quiz already finished")
        with(currentQuestion) {
            val aiAnswer = chatClient.prompt().user(
                """
                Choose the correctOption for the following question: "${question}"
                With the hint: "${hint}"
                The options are: 
                OptionA:${optionA},
                OptionB:${optionB},
                OptionC:${optionC},
                OptionD:${optionD}.
                The user selected ${selectedOption}.
                Also give an explanation why this is the correctOption. If the user selectedOption is wrong, also add it to the explanation.
            """.trimIndent()
            ).call().entity<Answer>()

            this.selectedOption = selectedOption
            correctOption = aiAnswer.correctOption
            explanation = aiAnswer.explanation
            save(quiz)

            return aiAnswer
        }
    }

    @Transactional
    private fun save(quiz: Quiz): Quiz =
        quiz.run(QuizToQuizEntityMapper)
            .run(quizRepository::save)
            .run(QuizEntityToQuizMapper)
}