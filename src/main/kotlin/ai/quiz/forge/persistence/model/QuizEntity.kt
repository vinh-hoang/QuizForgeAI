package ai.quiz.forge.persistence.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "quiz")
class QuizEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    val topic: String,

    @OneToMany(mappedBy = "quiz", cascade = [CascadeType.ALL], orphanRemoval = true)
    val questions: MutableList<QuestionEntity> = mutableListOf(),

    @Column(name = "created_at")
    @CreationTimestamp
    val createdAt: ZonedDateTime? = null,
)
