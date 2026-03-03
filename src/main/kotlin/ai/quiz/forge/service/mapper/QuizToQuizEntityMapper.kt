package ai.quiz.forge.service.mapper

import ai.quiz.forge.persistence.model.QuestionEntity
import ai.quiz.forge.persistence.model.QuizEntity
import ai.quiz.forge.service.model.Quiz

object QuizToQuizEntityMapper : (Quiz) -> QuizEntity {
    override fun invoke(quiz: Quiz): QuizEntity {
        val quizEntity = QuizEntity(
            topic = quiz.topic,
        )
        quiz.questions.forEachIndexed { index, q ->
            quizEntity.questions.add(
                with(q) {
                    QuestionEntity(
                        quiz = quizEntity,
                        question = question,
                        optionA = optionA,
                        optionB = optionB,
                        optionC = optionC,
                        optionD = optionD,
                        correctOption = correctOption,
                        selectedOption = selectedOption,
                        hint = hint,
                        position = index,
                    )
                }
            )
        }
        return quizEntity
    }
}
