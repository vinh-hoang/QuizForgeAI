package ai.quiz.forge.service.model.ai.generated

import ai.quiz.forge.service.model.Question
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class NewQuiz(
    val topic: String,
    val questions: List<Question>,
) {
    override fun toString(): String = objectMapper.writeValueAsString(this)

    private companion object {
        val objectMapper: ObjectMapper by lazy { jacksonObjectMapper() }
    }
}
