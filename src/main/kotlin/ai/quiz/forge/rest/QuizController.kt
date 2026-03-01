package ai.quiz.forge.rest

import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class QuizController(
    val chatClient: ChatClient
) {
    @PostMapping("/chat")
    fun hello(@RequestBody input: String): String {
        return chatClient.prompt(input).call().content() ?: "Error: No response from the model"
    }
}