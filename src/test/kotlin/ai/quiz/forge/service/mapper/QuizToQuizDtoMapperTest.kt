package ai.quiz.forge.service.mapper

import ai.quiz.forge.service.model.Question
import ai.quiz.forge.service.model.Quiz
import ai.quiz.forge.shared.Option
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class QuizToQuizDtoMapperTest {

    @Test
    fun `maps quiz to dto using first unanswered question as currentQuestion`() {
        val quiz = Quiz(
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
                Question(
                    question = "Q2?",
                    optionA = "A2",
                    optionB = "B2",
                    optionC = "C2",
                    optionD = "D2",
                    correctOption = Option.OPTION_B,
                    hint = "H2",
                ),
            ),
        )

        val dto = QuizToQuizDtoMapper(quiz)

        assertEquals(0, dto.id)
        assertEquals("NEW", dto.status)
        assertEquals(2, dto.questionCount)
        assertEquals(2, dto.currentQuestion.position)
        assertEquals("Q2?", dto.currentQuestion.question)
        assertEquals("A2", dto.currentQuestion.optionA)
        assertEquals("H2", dto.currentQuestion.hint)
    }

    @Test
    fun `throws when quiz has no questions`() {
        val quiz = Quiz(topic = "Empty", questions = emptyList())
        assertThrows(IllegalArgumentException::class.java) {
            QuizToQuizDtoMapper(quiz)
        }
    }

    @Test
    fun `throws when all questions already answered`() {
        val quiz = Quiz(
            topic = "Answered",
            questions = listOf(
                Question(
                    question = "Q1?",
                    optionA = "A",
                    optionB = "B",
                    optionC = "C",
                    optionD = "D",
                    correctOption = Option.OPTION_A,
                    selectedOption = Option.OPTION_A,
                    hint = "H",
                ),
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            QuizToQuizDtoMapper(quiz)
        }
    }
}
