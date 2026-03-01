package ai.quiz.forge.rest

import ai.quiz.forge.rest.model.CreateQuiz
import ai.quiz.forge.service.QuizPersistenceService
import ai.quiz.forge.service.model.Quiz
import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class QuizController(
    val chatClient: ChatClient,
    val quizPersistenceService: QuizPersistenceService,
) {
    @PostMapping("/chat")
    fun hello(@RequestBody createQuiz: CreateQuiz): String {
        val quiz = chatClient.prompt().user(
            """
            Take a deep breath. Create a Quiz about the topic "${createQuiz.topic}".
            The quiz should have ${createQuiz.numberOfQuestions.toString().lowercase()} questions
            and be of ${createQuiz.difficulty.toString().lowercase()} difficulty.
            The questions should have exactly 4 options and only one correct answer.
            Please check the correctness of the answers to avoid mistakes.
            Give a subtle Hint for each question if the user struggles.
            """.trimIndent()
        ).call().entity(Quiz::class.java)
        if (quiz != null){
            quizPersistenceService.save(quiz)
            return quiz.toString()
        }
        return "Failed to generate quiz"
    }
}