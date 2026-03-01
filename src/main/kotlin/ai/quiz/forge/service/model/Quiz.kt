package ai.quiz.forge.service.model

data class Quiz(
    val topic: String,
    val questions: List<Question>,
)
