package ai.quiz.forge.rest.model

data class CreateQuiz(
    val topic: String,
    val numberOfQuestions: NumberOfQuestions,
    val difficulty: Difficulty
) {
    enum class Difficulty {
        BEGINNER,
        ADVANCED,
        EXPERT
    }

    enum class NumberOfQuestions {
        FIVE,
        TEN,
        FIFTEEN
    }
}