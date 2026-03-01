package ai.quiz.forge

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class AiProjectApplication

fun main(args: Array<String>) {
	runApplication<AiProjectApplication>(*args)
}

@RestController
class HelloController(model: ChatModel) {

	val chat: ChatClient = ChatClient.builder(model).build()

	@PostMapping("/chat")
	fun hello(@RequestBody input: String): String {
		return chat.prompt(input).call().content()?:"Error: No response from the model"
	}
}