import { computed, ref } from 'vue'
import { answerQuestion, createQuiz, getQuiz } from '../api/quizApi'
import type {
  AnswerResponse,
  CreateQuizRequest,
  Option,
  QuizDto,
  ReviewItem,
} from '../types/quiz'

export type QuizPhase = 'setup' | 'generating' | 'question' | 'feedback' | 'review'

export function useQuiz() {
  const phase = ref<QuizPhase>('setup')
  const quiz = ref<QuizDto | null>(null)
  const review = ref<ReviewItem[]>([])
  const selectedOption = ref<Option | null>(null)
  const answer = ref<AnswerResponse | null>(null)
  const errorMessage = ref<string | null>(null)
  const isSubmitting = ref(false)
  const isLoadingNext = ref(false)

  const isLastQuestion = computed(() => {
    if (!quiz.value) {
      return false
    }

    return quiz.value.currentQuestionIndex >= quiz.value.questionCount
  })

  async function startQuiz(payload: CreateQuizRequest) {
    if (!payload.topic.trim()) {
      errorMessage.value = 'Add a topic so the forge knows what to build.'
      return
    }

    phase.value = 'generating'
    errorMessage.value = null
    quiz.value = null
    review.value = []
    selectedOption.value = null
    answer.value = null

    try {
      quiz.value = await createQuiz(payload)
      phase.value = 'question'
    } catch (error) {
      errorMessage.value = getErrorMessage(error, 'The quiz could not be created. Try again.')
      phase.value = 'setup'
    }
  }

  function selectOption(option: Option) {
    if (phase.value !== 'question' || isSubmitting.value) {
      return
    }

    selectedOption.value = option
  }

  async function submitAnswer() {
    if (!quiz.value || !selectedOption.value || phase.value !== 'question') {
      return
    }

    isSubmitting.value = true
    errorMessage.value = null
    const currentQuestion = quiz.value.currentQuestion
    const submittedOption = selectedOption.value

    try {
      answer.value = await answerQuestion(quiz.value.id, submittedOption)
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
        correctOption: answer.value.correctOption,
        explanation: answer.value.explanation,
        isCorrect: submittedOption === answer.value.correctOption,
      })
      phase.value = 'feedback'
    } catch (error) {
      errorMessage.value = getErrorMessage(error, 'Your answer could not be checked. Try again.')
    } finally {
      isSubmitting.value = false
    }
  }

  async function nextQuestion() {
    if (!quiz.value || phase.value !== 'feedback') {
      return
    }

    if (isLastQuestion.value) {
      phase.value = 'review'
      return
    }

    isLoadingNext.value = true
    errorMessage.value = null

    try {
      quiz.value = await getQuiz(quiz.value.id)
      selectedOption.value = null
      answer.value = null
      phase.value = 'question'
    } catch (error) {
      errorMessage.value = getErrorMessage(error, 'The next question could not be loaded.')
    } finally {
      isLoadingNext.value = false
    }
  }

  function reset() {
    phase.value = 'setup'
    quiz.value = null
    review.value = []
    selectedOption.value = null
    answer.value = null
    errorMessage.value = null
    isSubmitting.value = false
    isLoadingNext.value = false
  }

  return {
    answer,
    errorMessage,
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

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof TypeError) {
    return 'The quiz forge is offline. Make sure Spring Boot is running on port 8080.'
  }

  if (error instanceof Error && error.message) {
    return error.message
  }

  return fallback
}