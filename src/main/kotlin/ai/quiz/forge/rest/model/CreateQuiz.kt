package ai.quiz.forge.rest.model

data class CreateQuiz(
    val topic: String,
    val numberOfQuestions: NumberOfQuestions,
    val difficulty: Difficulty
) {
    enum class Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    enum class NumberOfQuestions {
        THREE,
        FIVE,
        SEVEN
    }
}