package ai.quiz.forge.persistence.repository

import ai.quiz.forge.persistence.entity.QuizEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface QuizRepository : JpaRepository<QuizEntity, UUID> {
    @EntityGraph(attributePaths = ["questions"])
    fun findWithQuestionsById(id: UUID): QuizEntity?
}

