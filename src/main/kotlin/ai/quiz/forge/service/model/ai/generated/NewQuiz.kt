package ai.quiz.forge.service.model.ai.generated

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class NewQuiz(
    val questions: List<NewQuestion>,
) {
    override fun toString(): String = objectMapper.writeValueAsString(this)

    private companion object {
        val objectMapper: ObjectMapper by lazy { jacksonObjectMapper() }
    }
}
