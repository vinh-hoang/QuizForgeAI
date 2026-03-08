package ai.quiz.forge.service

import ai.quiz.forge.service.model.Question
import ai.quiz.forge.service.model.Quiz
import ai.quiz.forge.shared.Option
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class QuizServiceIT {

    @Autowired
    lateinit var quizPersistenceService: QuizService

    @Test
    fun `saves and loads quiz`() {
        val quiz = Quiz(
            topic = "Kotlin basics",
            questions = listOf(
                Question(
                    question = "Which keyword declares an immutable variable?",
                    optionA = "var",
                    optionB = "val",
                    optionC = "let",
                    optionD = "const",
                    correctOption = Option.OPTION_B,
                    hint = "Think 'value'.",
                ),
                Question(
                    question = "What is the file extension for Kotlin source files?",
                    optionA = ".kt",
                    optionB = ".kts",
                    optionC = ".kotlin",
                    optionD = ".java",
                    correctOption = Option.OPTION_A,
                    hint = "Two letters.",
                ),
            ),
        )

        val saved = quizPersistenceService.save(quiz)
        val id = saved.id
        assertNotNull(id)

        val loaded = quizPersistenceService.findById(id!!)

        assertNotNull(loaded)
        assertEquals(quiz.topic, loaded!!.topic)
        assertEquals(quiz.questions, loaded.questions)
    }
}
