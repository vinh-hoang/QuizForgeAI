package ai.quiz.forge.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class QuizServiceIT {

    @Autowired
    lateinit var quizService: QuizService

    @Test
    fun `getQuiz throws not found for unknown id`() {
        val unknownId = UUID.randomUUID()

        val ex = assertThrows(ResponseStatusException::class.java) {
            quizService.getQuiz(unknownId)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
        assertEquals("Quiz with id $unknownId not found", ex.reason)
    }
}
