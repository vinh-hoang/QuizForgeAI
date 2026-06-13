package ai.quiz.forge.rest.model

import java.util.UUID

data class QuizDto(
    val id: UUID,
    val questionCount: Int,
    /** Zero-based index of the current (first unanswered) question. */
    val currentQuestionIndex: Int,
    val currentQuestion: QuestionDto,
) {
    data class QuestionDto(
        val question: String,
        val optionA: String,
        val optionB: String,
        val optionC: String,
        val optionD: String,
        val hint: String,
    )
}