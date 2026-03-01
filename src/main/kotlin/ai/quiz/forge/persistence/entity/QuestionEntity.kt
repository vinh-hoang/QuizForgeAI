package ai.quiz.forge.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "question")
data class QuestionEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    val quiz: QuizEntity,

    val question: String,

    @Column(name = "option_a")
    val optionA: String,

    @Column(name = "option_b")
    val optionB: String,

    @Column(name = "option_c")
    val optionC: String,

    @Column(name = "option_d")
    val optionD: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "correct_answer")
    val correctAnswer: CorrectAnswer,

    val hint: String,
    val position: Int,
) {
    enum class CorrectAnswer {
        OPTION_A,
        OPTION_B,
        OPTION_C,
        OPTION_D,
    }
}
