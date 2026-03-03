package ai.quiz.forge.service.mapper

import ai.quiz.forge.service.model.Question
import ai.quiz.forge.service.model.ai.generated.NewQuiz
import ai.quiz.forge.shared.Option
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class NewQuizToQuizMapperTest {

    @Test
    fun `maps newQuiz to quiz and clears any selectedOption`() {
        val newQuiz = NewQuiz(
            topic = "Kotlin",
            questions = listOf(
                Question(
                    question = "Q1?",
                    optionA = "A",
                    optionB = "B",
                    optionC = "C",
                    optionD = "D",
                    correctOption = Option.OPTION_A,
                    selectedOption = Option.OPTION_B,
                    hint = "H1",
                ),
            ),
        )

        val quiz = NewQuizToQuizMapper(newQuiz)

        assertNotNull(quiz.id)
        assertEquals("Kotlin", quiz.topic)
        assertEquals(1, quiz.questions.size)
        assertEquals("Q1?", quiz.questions[0].question)
        assertNull(quiz.questions[0].selectedOption)
    }

    @Test
    fun `throws when newQuiz has no questions`() {
        val newQuiz = NewQuiz(topic = "Empty", questions = emptyList())
        assertThrows(IllegalArgumentException::class.java) {
            NewQuizToQuizMapper(newQuiz)
        }
    }
}

