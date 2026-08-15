package ai.quiz.forge

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class QuizForgeAiApplicationTests @Autowired constructor(
	private val model: OpenAiChatModel
) {

	@Test
	fun contextLoads() {
	}

	@Test
	fun configuresLowReasoningEffort() {
		assertEquals("low", model.options.reasoningEffort)
		assertNull(model.options.extraBody)
	}

}