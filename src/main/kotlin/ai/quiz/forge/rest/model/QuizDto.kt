package ai.quiz.forge.rest.model

data class QuizDto(
    val id: Int,
    val status: String,
    val questionCount: Int,
    val currentQuestion: Question
){
    data class Question(
        val position: Int
    )
}