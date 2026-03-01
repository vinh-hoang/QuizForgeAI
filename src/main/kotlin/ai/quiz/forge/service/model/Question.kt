package ai.quiz.forge.service.model

import com.fasterxml.jackson.annotation.JsonProperty

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
        OPTION_D;

        @JsonProperty
        fun value(input: String): CorrectAnswer = valueOf(input)
    }
}
