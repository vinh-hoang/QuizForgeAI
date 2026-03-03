package ai.quiz.forge.service.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID

data class Quiz(
    val id: UUID = UUID.randomUUID(),
    val topic: String,
    val questions: List<Question>,
) {
    override fun toString(): String = objectMapper.writeValueAsString(this)

    private companion object {
        val objectMapper: ObjectMapper by lazy { jacksonObjectMapper() }
    }
}
