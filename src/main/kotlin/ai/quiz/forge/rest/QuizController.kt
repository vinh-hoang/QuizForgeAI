package ai.quiz.forge.rest

import ai.quiz.forge.rest.model.CreateQuiz
import ai.quiz.forge.service.QuizPersistenceService
import ai.quiz.forge.service.model.Quiz
import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
class QuizController(
    val chatClient: ChatClient,
    val quizPersistenceService: QuizPersistenceService,
) {
    @PostMapping("/quiz")
    fun createQuiz(@RequestBody createQuiz: CreateQuiz): String {
        val quiz = chatClient.prompt().user(
            """
            Take a deep breath. Create a Quiz about the topic "${createQuiz.topic}".
            The quiz should have ${createQuiz.numberOfQuestions.toString().lowercase()} questions
            and be of ${createQuiz.difficulty.toString().lowercase()} difficulty.
            Try to have specific questions and not only general ones.
            The questions should have exactly 4 options and only one correct option.
            Make sure that the other options are incorrect.
            Please be careful of the correctness of the option to avoid mistakes.
            The Hint should have very very specific Information and should not repeat the information given from the question itself.
            """.trimIndent()
        ).call().entity(Quiz::class.java)

        if (quiz == null) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to generate quiz",
            )
        }

        quizPersistenceService.save(quiz)
        return quiz.toString()
    }

    @GetMapping("/quiz/{quizId}")
    fun getQuiz(@PathVariable quizId: UUID): Quiz =
        quizPersistenceService.findById(quizId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Quiz with id $quizId not found",
            )

    @PostMapping("/quiz/{quizId}/question/{questionId}/answer")
    fun answerQuestion(
        @PathVariable quizId: UUID,
        @PathVariable questionId: UUID,
        @RequestBody body: String
    ) {
        // TODO implement answer submission
    }
}
