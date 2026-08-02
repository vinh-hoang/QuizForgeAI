package ai.quiz.forge.service

import ai.quiz.forge.persistence.repository.QuestionRepository
import ai.quiz.forge.persistence.repository.QuizRepository
import ai.quiz.forge.service.mapper.QuizEntityToQuizMapper
import ai.quiz.forge.service.mapper.QuizToQuizEntityMapper
import ai.quiz.forge.service.model.Quiz
import ai.quiz.forge.shared.Option
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class QuizPersistenceService(
    private val quizRepository: QuizRepository,
    private val questionRepository: QuestionRepository,
) {

    @Transactional
    fun save(quiz: Quiz): Quiz =
        quiz.run(QuizToQuizEntityMapper)
            .run(quizRepository::save)
            .run(QuizEntityToQuizMapper)

    @Transactional
    fun answerQuestion(
        quizId: UUID,
        position: Int,
        selectedOption: Option,
        correctOption: Option,
        explanation: String,
    ): Boolean =
        questionRepository.answerCurrentQuestion(
            quizId = quizId,
            position = position,
            selectedOption = selectedOption,
            correctOption = correctOption,
            explanation = explanation,
        ) == 1
}
