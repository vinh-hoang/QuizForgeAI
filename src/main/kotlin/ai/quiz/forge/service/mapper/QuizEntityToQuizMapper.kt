package ai.quiz.forge.service.mapper

import ai.quiz.forge.persistence.model.QuestionEntity
import ai.quiz.forge.persistence.model.QuizEntity
import ai.quiz.forge.service.model.Question
import ai.quiz.forge.service.model.Quiz


object QuizEntityToQuizMapper : (QuizEntity) -> Quiz {

    override fun invoke(entity: QuizEntity): Quiz {
        val questions = entity.questions
            .sortedBy { it.position }
            .map { it.toQuestion() }

        return Quiz(
            topic = entity.topic,
            questions = questions,
        )
    }

    private fun QuestionEntity.toQuestion(): Question =
        Question(
            question = question,
            optionA = optionA,
            optionB = optionB,
            optionC = optionC,
            optionD = optionD,
            correctOption = correctOption,
            hint = hint,
        )
}
