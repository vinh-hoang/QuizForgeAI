package ai.quiz.forge.rest

import ai.quiz.forge.rest.model.CreateQuiz
import ai.quiz.forge.rest.model.QuizDto
import ai.quiz.forge.service.QuizPersistenceService
import ai.quiz.forge.service.mapper.NewQuizToQuizMapper
import ai.quiz.forge.service.mapper.QuizEntityToQuizMapper
import ai.quiz.forge.service.mapper.QuizToQuizDtoMapper
import ai.quiz.forge.service.model.Quiz
import ai.quiz.forge.service.model.ai.generated.NewQuiz
import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
    fun createQuiz(@RequestBody createQuiz: CreateQuiz): QuizDto {
        val newQuiz = chatClient.prompt().user(
            """
            Create a Quiz about the topic "${createQuiz.topic}".
            The quiz should have ${createQuiz.numberOfQuestions.toString().lowercase()} questions
            and be of ${createQuiz.difficulty.toString().lowercase()} difficulty.
            The questions should have exactly 4 options and only one correct option.
            The Hint should have very specific Information and should not repeat the information given from the question itself.
            """.trimIndent()
        ).call().entity(NewQuiz::class.java)

        if (newQuiz == null) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to generate quiz",
            )
        }

        val generatedQuiz = NewQuizToQuizMapper(newQuiz)
        val savedQuiz = quizPersistenceService.save(generatedQuiz)
        return savedQuiz
            .run(QuizEntityToQuizMapper)
            .run(QuizToQuizDtoMapper)
    }

    @GetMapping("/quiz/{quizId}")
    fun getQuiz(@PathVariable quizId: UUID): QuizDto =
        quizPersistenceService.findById(quizId)
            ?.run(QuizToQuizDtoMapper)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Quiz with id $quizId not found",
            )

    @PatchMapping("/quiz/{quizId}/question/current")
    fun answerQuestion(
        @PathVariable quizId: UUID,
        @RequestBody body: String
    ) {
        // TODO implement answer submission
    }
}
