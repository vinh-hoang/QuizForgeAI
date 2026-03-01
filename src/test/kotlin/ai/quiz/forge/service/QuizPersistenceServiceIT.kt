package ai.quiz.forge.service

import ai.quiz.forge.service.model.Question
import ai.quiz.forge.service.model.Quiz
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class QuizPersistenceServiceIT {

    @Autowired
    lateinit var quizPersistenceService: QuizPersistenceService

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
                    correctAnswer = Question.CorrectAnswer.OPTION_B,
                    hint = "Think 'value'.",
                ),
                Question(
                    question = "What is the file extension for Kotlin source files?",
                    optionA = ".kt",
                    optionB = ".kts",
                    optionC = ".kotlin",
                    optionD = ".java",
                    correctAnswer = Question.CorrectAnswer.OPTION_A,
                    hint = "Two letters.",
                ),
            ),
        )

        val id = quizPersistenceService.save(quiz)
        val loaded = quizPersistenceService.findById(id)

        assertNotNull(loaded)
        assertEquals(quiz.topic, loaded!!.topic)
        assertEquals(quiz.questions, loaded.questions)
    }
}
