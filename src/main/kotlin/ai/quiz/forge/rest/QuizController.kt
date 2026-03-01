package ai.quiz.forge.rest

import ai.quiz.forge.rest.model.CreateQuiz
import ai.quiz.forge.service.model.Quiz
import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class QuizController(
    val chatClient: ChatClient
) {
    @PostMapping("/chat")
    fun hello(@RequestBody createQuiz: CreateQuiz): String {
        val quiz = chatClient.prompt().user(
            "Create a Quiz about the topic \"${createQuiz.topic}\"."
                    + "The quiz should have ${createQuiz.numberOfQuestions} questions and be of ${createQuiz.difficulty} difficulty."
        ).call().entity(Quiz::class.java)
        return quiz.toString()
    }
}