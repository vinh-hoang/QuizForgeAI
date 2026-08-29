package ai.quiz.forge.service

import ai.quiz.forge.persistence.repository.QuizRepository
import ai.quiz.forge.rest.model.CreateQuiz
import ai.quiz.forge.service.mapper.QuizEntityToQuizMapper
import ai.quiz.forge.service.model.Question
import ai.quiz.forge.service.model.Quiz
import ai.quiz.forge.service.model.ai.generated.Answer
import ai.quiz.forge.service.model.ai.generated.NewQuestion
import ai.quiz.forge.shared.Option
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.ChatClientAttributes
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class QuizService(
    private val quizRepository: QuizRepository,
    private val chatClient: ChatClient,
    private val quizPersistenceService: QuizPersistenceService,
) {

    private companion object {
        private val log = LoggerFactory.getLogger(QuizService::class.java)
    }

    private fun CreateQuiz.NumberOfQuestions.toInt(): Int = when (this) {
        CreateQuiz.NumberOfQuestions.THREE -> 3
        CreateQuiz.NumberOfQuestions.FIVE -> 5
        CreateQuiz.NumberOfQuestions.SEVEN -> 7
    }

    fun createQuiz(createQuiz: CreateQuiz): Quiz {
        val topic = createQuiz.topic
        val totalQuestions = createQuiz.numberOfQuestions.toInt()
        val difficulty = createQuiz.difficulty.toString().lowercase()
        val generatedQuestions = mutableListOf<NewQuestion>()

        repeat(totalQuestions) { index ->
            val previousQuestionsPrompt = if (generatedQuestions.isNotEmpty()) {
                "Do NOT generate a question similar to these:\n" +
                        generatedQuestions.joinToString("\n") { "- ${it.question}" }
            } else {
                ""
            }

            val prompt = """
                Create a single quiz question about the topic "$topic" of $difficulty difficulty.
                Keep the question concise and no longer than 30 words.
                The question should have exactly 4 answer options and only one correct option.
                The Hint should help to find the correct option.
                Check if the question makes sense and is free of errors.
                $previousQuestionsPrompt
            """.trimIndent()

            val newQuestion = generateQuestion(prompt, index + 1, totalQuestions)
            generatedQuestions.add(newQuestion)
        }

        return Quiz(
            topic = topic,
            questions = generatedQuestions.map {
                Question(
                    question = it.question,
                    optionA = it.optionA,
                    optionB = it.optionB,
                    optionC = it.optionC,
                    optionD = it.optionD,
                    hint = it.hint,
                )
            },
        ).run(quizPersistenceService::save)
    }

    private fun generateQuestion(prompt: String, questionNumber: Int, totalQuestions: Int): NewQuestion {
        repeat(5) {
            try {
                val rawQuestionDraft = generateQuestionDraft(prompt)
                return chatClient.prompt()
                    .user(buildQuestionStructuringPrompt(rawQuestionDraft))
                    .call().entity(NewQuestion::class.java)
                    ?: throw IllegalStateException("AI returned no quiz question")
            } catch (e: Exception) {
                log.warn("Error occurred while generating new question", e)
            }
        }
        throw RuntimeException("Failed to generate question #$questionNumber of $totalQuestions after 5 attempts")
    }

    private fun generateQuestionDraft(prompt: String): String {
        val rawQuestionDraft = chatClient.prompt()
            .advisors { advisorSpec ->
                advisorSpec.param(ChatClientAttributes.STRUCTURED_OUTPUT_NATIVE.key, false)
            }
            .user(prompt)
            .call()
            .content()
            ?: throw IllegalStateException("AI returned no quiz question draft")

        if (rawQuestionDraft.isBlank()) {
            throw IllegalStateException("AI returned blank quiz question draft")
        }

        return rawQuestionDraft
    }

    private fun buildQuestionStructuringPrompt(rawQuestionDraft: String): String =
        """
        You are a strict quiz data extraction expert. Convert the draft below into the NewQuestion schema fields.

        Follow these field rules exactly:
        - question: include only the question itself. Do not include a "Question:" label, answer choices, option labels, a solution, an explanation, or the hint.
        - optionA, optionB, optionC, optionD: include exactly one answer choice per field. Remove labels such as "A)", "B)", "C)", "D)", "Option A:", or "Option B:".
        - hint: include only the hint itself. Remove a "Hint:" label and do not include the answer or an explanation.
        - Do not duplicate answer choices or hint text in the question field.
        - Preserve necessary wording and any LaTeX math from the draft in the field where it belongs.

        Return only values for the schema fields. Do not add commentary or combine multiple fields.

        <quiz-question-draft>
        """.trimIndent() +
                "\n" +
                rawQuestionDraft +
                "\n</quiz-question-draft>"

    @Transactional(readOnly = true)
    fun getQuiz(id: UUID): Quiz =
        quizRepository.findWithQuestionsById(id)
            ?.run(QuizEntityToQuizMapper)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz with id $id not found")

    fun answerQuestion(quizId: UUID, selectedOption: Option): Answer {
        val quiz = getQuiz(quizId)

        /**
         * It seems like the AI should answer each question separately to archive better results.
         **/
        val currentQuestionIndex = quiz.questions.indexOfFirst { it.selectedOption == null }
        if (currentQuestionIndex < 0) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Quiz already finished")
        }
        val currentQuestion = quiz.questions[currentQuestionIndex]

        val aiAnswer = processQuestionAnswer(currentQuestion, selectedOption)

        val updated = quizPersistenceService.answerQuestion(
            quizId = quizId,
            position = currentQuestionIndex,
            selectedOption = selectedOption,
            correctOption = aiAnswer.correctOption,
            explanation = aiAnswer.explanation,
        )
        if (!updated) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Question was already answered",
            )
        }

        return aiAnswer
    }

    /**
     * Processes the AI request for a single question answer.
     */
    private fun processQuestionAnswer(currentQuestion: Question, selectedOption: Option): Answer {
        val prompt = buildAnswerPrompt(currentQuestion, selectedOption)
        return chatClient.prompt().user(prompt)
            .call().entity(Answer::class.java)
            ?: throw IllegalStateException("AI returned no quiz answer")
    }

    /**
     * Builds the detailed prompt for the AI based on the current question's state.
     */
    private fun buildAnswerPrompt(currentQuestion: Question, selectedOption: Option): String {
        return """
                Choose the correctOption for the following question: "${currentQuestion.question}"
                With the hint: "${currentQuestion.hint}"
                The options are: 
                OptionA:${currentQuestion.optionA},
                OptionB:${currentQuestion.optionB},
                OptionC:${currentQuestion.optionC},
                OptionD:${currentQuestion.optionD}.
                The user selected $selectedOption.
                Also give an explanation why this is the correctOption. If the user selectedOption is wrong, also add it to the explanation.
                Keep the explanation concise.
            """.trimIndent()
    }

}
