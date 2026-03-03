package ai.quiz.forge.service

import ai.quiz.forge.persistence.model.QuizEntity
import ai.quiz.forge.persistence.repository.QuizRepository
import ai.quiz.forge.service.mapper.QuizEntityToQuizMapper
import ai.quiz.forge.service.mapper.QuizToQuizEntityMapper
import ai.quiz.forge.service.model.Quiz
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class QuizPersistenceService(
    private val quizRepository: QuizRepository,
) {
    @Transactional
    fun save(quiz: Quiz): QuizEntity {
        val quizEntity = QuizToQuizEntityMapper(quiz)
        return quizRepository.save(quizEntity)
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): Quiz? {
        val entity = quizRepository.findById(id)
        return if (entity.isPresent) {
            QuizEntityToQuizMapper(entity.get())
        } else {
            null
        }
    }
}
