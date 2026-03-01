package ai.quiz.forge.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID


@Entity
@Table(name = "quiz")
data class QuizEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    val topic: String,

    @Column(name = "created_at")
    val createdAt: OffsetDateTime,

    @OneToMany(mappedBy = "quiz", cascade = [CascadeType.ALL], orphanRemoval = true)
    val questions: MutableList<QuestionEntity> = mutableListOf(),
)
