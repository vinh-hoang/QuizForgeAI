package ai.quiz.forge.persistence.repository

import ai.quiz.forge.persistence.model.QuestionEntity
import ai.quiz.forge.shared.Option
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface QuestionRepository : JpaRepository<QuestionEntity, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update QuestionEntity q
           set q.selectedOption = :selectedOption,
               q.correctOption = :correctOption,
               q.explanation = :explanation
         where q.quiz.id = :quizId
           and q.position = :position
           and q.selectedOption is null
        """
    )
    fun answerCurrentQuestion(
        @Param("quizId") quizId: UUID,
        @Param("position") position: Int,
        @Param("selectedOption") selectedOption: Option,
        @Param("correctOption") correctOption: Option,
        @Param("explanation") explanation: String,
    ): Int
}
