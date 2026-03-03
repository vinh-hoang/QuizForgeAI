package ai.quiz.forge.rest.model

import java.util.UUID

data class QuizDto(
    val id: UUID,
    val questionCount: Int,
    val currentQuestion: QuestionDto
) {
    data class QuestionDto(
        val position: Int, //starting at 1
        val question: String,
        val optionA: String,
        val optionB: String,
        val optionC: String,
        val optionD: String,
        val hint: String,
    )
}