package ai.quiz.forge.config

import org.springframework.ai.chat.client.AdvisorParams
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ChatClientConfig(
    val model: OpenAiChatModel
) {

    @Bean
    fun chatClient() = ChatClient.builder(model)
        .defaultAdvisors(loggerAdvisor())
        .defaultAdvisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
        .build()

    private fun loggerAdvisor() = SimpleLoggerAdvisor(
        { request -> "Request: " + request!!.prompt().getUserMessage().text },
        { response -> "Response: " + response!!.getResult()?.output?.text },
        0
    )
}