package ai.quiz.forge.service.mapper

import ai.quiz.forge.rest.model.QuizDto
import ai.quiz.forge.service.model.Quiz

object QuizToQuizDtoMapper : (Quiz) -> QuizDto {
    override fun invoke(quiz: Quiz): String {
        val currentIndex = quiz.questions.indexOfFirst { question -> question.selectedOption == null }
        val currentQuestion = quiz.questions[currentIndex]

        return QuizDto(
            // Service model Quiz currently doesn't carry persistence id/status.
            // These placeholders keep the DTO usable until the API model is aligned.
            id = quiz.id,
            questionCount = quiz.questions.size,
            currentQuestion = QuizDto.QuestionDto(
                // DTO contract says position starts at 1
                position = currentIndex + 1,
                question = currentQuestion.question,
                optionA = currentQuestion.optionA,
                optionB = currentQuestion.optionB,
                optionC = currentQuestion.optionC,
                optionD = currentQuestion.optionD,
                hint = currentQuestion.hint,
            ),
        )
    }
}