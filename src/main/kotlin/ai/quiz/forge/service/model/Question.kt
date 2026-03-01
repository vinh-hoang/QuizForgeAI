package ai.quiz.forge.service.model

data class Question (
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: CorrectAnswer,
) {
    enum class CorrectAnswer {
        OPTION_A,
        OPTION_B,
        OPTION_C,
        OPTION_D
    }
}