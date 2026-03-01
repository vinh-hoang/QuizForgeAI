package ai.quiz.forge.service.model

data class Question (
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: Options,
    val hint: String,
) {
    enum class Options {
        OPTION_A,
        OPTION_B,
        OPTION_C,
        OPTION_D;
    }
}
