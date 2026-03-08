package ai.quiz.forge.rest

import ai.quiz.forge.rest.model.AnswerDto
import ai.quiz.forge.rest.model.CreateQuiz
import ai.quiz.forge.rest.model.QuizDto
import ai.quiz.forge.service.QuizService
import ai.quiz.forge.service.mapper.QuizToQuizDtoMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class QuizController(
    val quizService: QuizService,
) {
    @PostMapping("/quiz")
    fun createQuiz(@RequestBody createQuiz: CreateQuiz): QuizDto =
        createQuiz
            .run(quizService::createQuiz)
            .run(QuizToQuizDtoMapper)

    @GetMapping("/quiz/{quizId}")
    fun getQuiz(@PathVariable quizId: UUID): QuizDto =
        quizId
            .run(quizService::getQuiz)
            .run(QuizToQuizDtoMapper)

    @PatchMapping("/quiz/{quizId}/question/current")
    fun answerQuestion(
        @PathVariable quizId: UUID,
        @RequestBody body: AnswerDto
    ) = quizService.answerQuestion(quizId, body.selectedOption)
}
