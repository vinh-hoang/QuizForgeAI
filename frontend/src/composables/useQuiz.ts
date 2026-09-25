import { computed, getCurrentScope, onScopeDispose, ref } from 'vue'
import { answerQuestion, ApiError, createQuiz, getQuiz } from '../api/quizApi'
import type {
  AnswerResponse,
  CreateQuizRequest,
  Option,
  QuizDto,
  ReviewItem,
} from '../types/quiz'

export type QuizPhase = 'setup' | 'generating' | 'question' | 'feedback' | 'review'

interface ActiveOperation {
  controller: AbortController
  generation: number
}

export function useQuiz() {
  const phase = ref<QuizPhase>('setup')
  const quiz = ref<QuizDto | null>(null)
  const review = ref<ReviewItem[]>([])
  const selectedOption = ref<Option | null>(null)
  const answer = ref<AnswerResponse | null>(null)
  const errorMessage = ref<string | null>(null)
  const errorStatus = ref<number | null>(null)
  const isSubmitting = ref(false)
  const isLoadingNext = ref(false)

  let sessionGeneration = 0
  const activeControllers = new Set<AbortController>()

  const isLastQuestion = computed(() => {
    if (!quiz.value) {
      return false
    }

    // QuizDto.currentQuestionIndex is explicitly one-based at the API boundary.
    return quiz.value.currentQuestionIndex >= quiz.value.questionCount
  })

  function invalidateOperations() {
    sessionGeneration += 1
    for (const controller of activeControllers) {
      controller.abort()
    }
    activeControllers.clear()
  }

  function beginOperation(): ActiveOperation {
    const operation = {
      controller: new AbortController(),
      generation: sessionGeneration,
    }
    activeControllers.add(operation.controller)
    return operation
  }

  function isCurrentOperation(operation: ActiveOperation): boolean {
    return (
      operation.generation === sessionGeneration &&
      activeControllers.has(operation.controller) &&
      !operation.controller.signal.aborted
    )
  }

  function finishOperation(operation: ActiveOperation) {
    activeControllers.delete(operation.controller)
  }

  function clearError() {
    errorMessage.value = null
    errorStatus.value = null
  }

  function setError(error: unknown, fallback: string) {
    errorStatus.value = error instanceof ApiError ? error.status : null
    errorMessage.value = getErrorMessage(error, fallback)
  }

  if (getCurrentScope()) {
    onScopeDispose(invalidateOperations)
  }

  async function startQuiz(payload: CreateQuizRequest) {
    if (phase.value === 'generating') {
      return
    }

    if (!payload.topic.trim()) {
      errorStatus.value = null
      errorMessage.value = 'Add a topic so the forge knows what to build.'
      return
    }

    invalidateOperations()
    phase.value = 'generating'
    clearError()
    quiz.value = null
    review.value = []
    selectedOption.value = null
    answer.value = null
    isSubmitting.value = false
    isLoadingNext.value = false

    const operation = beginOperation()

    try {
      const nextQuiz = await createQuiz(payload, { signal: operation.controller.signal })
      if (!isCurrentOperation(operation)) {
        return
      }

      quiz.value = nextQuiz
      phase.value = 'question'
    } catch (error) {
      if (!isCurrentOperation(operation) || isSilentCancellation(error)) {
        return
      }

      if (error instanceof ApiError && error.status === 422) {
        errorStatus.value = error.status
        errorMessage.value = 'Choose a recognizable subject or activity that can support a quiz.'
      } else {
        setError(error, 'The quiz could not be created. Try again.')
      }
      phase.value = 'setup'
    } finally {
      finishOperation(operation)
    }
  }

  function selectOption(option: Option) {
    if (phase.value !== 'question' || isSubmitting.value) {
      return
    }

    selectedOption.value = option
  }

  async function submitAnswer() {
    if (!quiz.value || !selectedOption.value || phase.value !== 'question' || isSubmitting.value) {
      return
    }

    const operation = beginOperation()
    isSubmitting.value = true
    clearError()
    const currentQuestion = quiz.value.currentQuestion
    const quizId = quiz.value.id
    const submittedOption = selectedOption.value

    try {
      const response = await answerQuestion(quizId, submittedOption, { signal: operation.controller.signal })
      if (!isCurrentOperation(operation)) {
        return
      }

      answer.value = response
      review.value.push({
        number: quiz.value.currentQuestionIndex,
        question: currentQuestion.question,
        options: {
          OPTION_A: currentQuestion.optionA,
          OPTION_B: currentQuestion.optionB,
          OPTION_C: currentQuestion.optionC,
          OPTION_D: currentQuestion.optionD,
        },
        selectedOption: submittedOption,
        correctOption: response.correctOption,
        explanation: response.explanation,
        isCorrect: submittedOption === response.correctOption,
      })
      phase.value = 'feedback'
    } catch (error) {
      if (!isCurrentOperation(operation) || isSilentCancellation(error)) {
        return
      }

      setError(error, 'Your answer could not be checked. Try again.')
    } finally {
      const current = isCurrentOperation(operation)
      finishOperation(operation)
      if (current) {
        isSubmitting.value = false
      }
    }
  }

  async function nextQuestion() {
    if (!quiz.value || phase.value !== 'feedback' || isLoadingNext.value) {
      return
    }

    if (isLastQuestion.value) {
      phase.value = 'review'
      clearError()
      return
    }

    const operation = beginOperation()
    isLoadingNext.value = true
    clearError()
    const quizId = quiz.value.id
    const currentQuestionIndex = quiz.value.currentQuestionIndex

    try {
      const nextQuiz = await getQuiz(quizId, { signal: operation.controller.signal })
      if (!isCurrentOperation(operation)) {
        return
      }

      if (nextQuiz.currentQuestionIndex <= currentQuestionIndex) {
        throw new ApiError('The server returned an invalid next question.', {
          kind: 'invalid-response',
        })
      }

      quiz.value = nextQuiz
      selectedOption.value = null
      answer.value = null
      phase.value = 'question'
    } catch (error) {
      if (!isCurrentOperation(operation) || isSilentCancellation(error)) {
        return
      }

      setError(error, 'The next question could not be loaded.')
    } finally {
      const current = isCurrentOperation(operation)
      finishOperation(operation)
      if (current) {
        isLoadingNext.value = false
      }
    }
  }

  function reset() {
    invalidateOperations()
    phase.value = 'setup'
    quiz.value = null
    review.value = []
    selectedOption.value = null
    answer.value = null
    clearError()
    isSubmitting.value = false
    isLoadingNext.value = false
  }

  return {
    answer,
    errorMessage,
    errorStatus,
    isLoadingNext,
    isSubmitting,
    phase,
    quiz,
    review,
    selectedOption,
    createQuiz: startQuiz,
    nextQuestion,
    reset,
    selectOption,
    submitAnswer,
  }
}

function isSilentCancellation(error: unknown): boolean {
  return error instanceof ApiError && error.kind === 'aborted'
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError) {
    if (error.kind === 'network') {
      return 'The quiz forge is offline. Make sure Spring Boot is running on port 8080.'
    }

    if (error.kind === 'timeout') {
      return `${error.message} Check the connection and try again.`
    }

    return error.message || fallback
  }

  if (error instanceof TypeError) {
    return 'The quiz forge is offline. Make sure Spring Boot is running on port 8080.'
  }

  if (error instanceof Error && error.message) {
    return error.message
  }

  return fallback
}
