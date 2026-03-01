package ai.quiz.forge.service.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class Quiz(
    val topic: String,
    val questions: List<Question>,
) {
    override fun toString(): String = objectMapper.writeValueAsString(this)

    private companion object {
        val objectMapper: ObjectMapper by lazy { jacksonObjectMapper() }
    }
}
