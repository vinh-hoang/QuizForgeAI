package ai.quiz.forge.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class LoggingAdvisor(
    val model: OpenAiChatModel
) {

    @Bean
    fun chatClient() = ChatClient.builder(model).defaultAdvisors(loggerAdvisor()).build()

    private fun loggerAdvisor() = SimpleLoggerAdvisor(
        { request -> "Custom request: " + request!!.prompt().getUserMessage().text },
        { response -> "Custom response: " + response!!.getResult()?.output?.text },
        0
    )
}