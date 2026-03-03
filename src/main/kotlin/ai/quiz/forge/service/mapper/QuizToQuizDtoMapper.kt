package ai.quiz.forge.service.mapper

import ai.quiz.forge.rest.model.QuizDto
import ai.quiz.forge.service.model.Quiz

object QuizToQuizDtoMapper : (Quiz) -> QuizDto {
    override fun invoke(quiz: Quiz): QuizDto {
        require(quiz.questions.isNotEmpty()) { "Quiz must contain at least one question" }

        val currentIndex = quiz.questions.indexOfFirst { question -> question.selectedOption == null }
        require(currentIndex >= 0) { "All questions are already answered" }

        val currentQuestion = quiz.questions[currentIndex]

        return QuizDto(
            id = quiz.id,
            questionCount = quiz.questions.size,
            currentQuestionIndex = currentIndex + 1,
            currentQuestion = QuizDto.QuestionDto(
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