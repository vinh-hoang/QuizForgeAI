import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useQuiz } from '../../src/composables/useQuiz'
import type { AnswerResponse, CreateQuizRequest, QuizDto } from '../../src/types/quiz'

const api = vi.hoisted(() => {
  class MockApiError extends Error {
    readonly kind: string
    readonly status: number | null

    constructor(message: string, options: { kind: string; status?: number | null }) {
      super(message)
      this.name = 'ApiError'
      this.kind = options.kind
      this.status = options.status ?? null
    }
  }

  return {
    ApiError: MockApiError,
    answerQuestion: vi.fn(),
    createQuiz: vi.fn(),
    getQuiz: vi.fn(),
  }
})

vi.mock('../../src/api/quizApi', () => api)

const payload: CreateQuizRequest = {
  topic: 'The Roman Republic',
  numberOfQuestions: 'THREE',
  difficulty: 'EASY',
}

const quiz = (currentQuestionIndex = 1): QuizDto => ({
  id: 'quiz-1',
  questionCount: 3,
  currentQuestionIndex,
  currentQuestion: {
    question: `Question ${currentQuestionIndex}`,
    optionA: 'Romulus',
    optionB: 'Caesar',
    optionC: 'Cicero',
    optionD: 'Augustus',
    hint: 'The answer starts with R.',
  },
})

const answer: AnswerResponse = {
  correctOption: 'OPTION_A',
  explanation: 'Romulus is the traditional founder.',
}

function deferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((promiseResolve, promiseReject) => {
    resolve = promiseResolve
    reject = promiseReject
  })
  return { promise, reject, resolve }
}

describe('useQuiz', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('moves through create, answer, next-question, and review phases', async () => {
    api.createQuiz.mockResolvedValue(quiz(1))
    api.answerQuestion.mockResolvedValue(answer)
    api.getQuiz.mockResolvedValue(quiz(2))

    const state = useQuiz()
    await state.createQuiz(payload)
    expect(state.phase.value).toBe('question')

    state.selectOption('OPTION_A')
    await state.submitAnswer()
    expect(state.phase.value).toBe('feedback')
    expect(state.review.value).toHaveLength(1)

    await state.nextQuestion()
    expect(state.phase.value).toBe('question')
    expect(state.quiz.value?.currentQuestionIndex).toBe(2)

    state.quiz.value = quiz(3)
    state.phase.value = 'feedback'
    await state.nextQuestion()
    expect(state.phase.value).toBe('review')
    expect(api.getQuiz).toHaveBeenCalledTimes(1)
  })

  it('ignores a create response that arrives after reset', async () => {
    const pending = deferred<QuizDto>()
    api.createQuiz.mockReturnValue(pending.promise)

    const state = useQuiz()
    const start = state.createQuiz(payload)
    expect(state.phase.value).toBe('generating')

    state.reset()
    pending.resolve(quiz())
    await start

    expect(state.phase.value).toBe('setup')
    expect(state.quiz.value).toBeNull()
    expect(state.review.value).toEqual([])
  })

  it('aborts the active create request and ignores a stale rejection', async () => {
    const pending = deferred<QuizDto>()
    api.createQuiz.mockReturnValue(pending.promise)

    const state = useQuiz()
    const start = state.createQuiz(payload)
    const requestOptions = api.createQuiz.mock.calls[0][1] as { signal: AbortSignal }

    state.reset()
    expect(requestOptions.signal.aborted).toBe(true)
    pending.reject(new api.ApiError('Request was cancelled.', { kind: 'aborted' }))
    await start

    expect(state.phase.value).toBe('setup')
    expect(state.errorMessage.value).toBeNull()
  })

  it('prevents duplicate quiz creation and maps timeout errors', async () => {
    const pending = deferred<QuizDto>()
    api.createQuiz.mockReturnValue(pending.promise)

    const state = useQuiz()
    const firstStart = state.createQuiz(payload)
    const duplicateStart = state.createQuiz(payload)
    expect(api.createQuiz).toHaveBeenCalledTimes(1)

    pending.resolve(quiz())
    await Promise.all([firstStart, duplicateStart])
    expect(state.phase.value).toBe('question')

    api.createQuiz.mockRejectedValueOnce(
      new api.ApiError('Request timed out after 300000 ms.', { kind: 'timeout' }),
    )
    await state.createQuiz(payload)

    expect(state.phase.value).toBe('setup')
    expect(state.errorMessage.value).toContain('Check the connection and try again')
  })

  it('maps network failures to an actionable offline message', async () => {
    api.createQuiz.mockRejectedValueOnce(
      new api.ApiError('The network request failed.', { kind: 'network' }),
    )

    const state = useQuiz()
    await state.createQuiz(payload)

    expect(state.phase.value).toBe('setup')
    expect(state.errorMessage.value).toContain('Spring Boot')
    expect(state.isSubmitting.value).toBe(false)
  })

  it('ignores an answer response after reset and blocks duplicate submits', async () => {
    api.createQuiz.mockResolvedValue(quiz())
    const pending = deferred<AnswerResponse>()
    api.answerQuestion.mockReturnValue(pending.promise)

    const state = useQuiz()
    await state.createQuiz(payload)
    state.selectOption('OPTION_A')

    const firstSubmit = state.submitAnswer()
    const duplicateSubmit = state.submitAnswer()
    expect(api.answerQuestion).toHaveBeenCalledTimes(1)

    state.reset()
    pending.resolve(answer)
    await Promise.all([firstSubmit, duplicateSubmit])

    expect(state.phase.value).toBe('setup')
    expect(state.answer.value).toBeNull()
    expect(state.review.value).toEqual([])
  })

  it('blocks duplicate next requests and exposes a 409 recovery state', async () => {
    api.createQuiz.mockResolvedValue(quiz())
    api.answerQuestion.mockResolvedValue(answer)
    const pending = deferred<QuizDto>()
    api.getQuiz.mockReturnValue(pending.promise)

    const state = useQuiz()
    await state.createQuiz(payload)
    state.selectOption('OPTION_A')
    await state.submitAnswer()

    const firstNext = state.nextQuestion()
    const duplicateNext = state.nextQuestion()
    expect(api.getQuiz).toHaveBeenCalledTimes(1)
    expect(state.isLoadingNext.value).toBe(true)

    pending.resolve(quiz(2))
    await Promise.all([firstNext, duplicateNext])
    expect(state.phase.value).toBe('question')

    api.answerQuestion.mockRejectedValueOnce(
      new api.ApiError('Quiz state conflict. Start a new quiz to recover.', {
        kind: 'http',
        status: 409,
      }),
    )
    state.selectOption('OPTION_A')
    await state.submitAnswer()

    expect(state.errorStatus.value).toBe(409)
    expect(state.errorMessage.value).toContain('Start a new quiz')
  })

  it('ignores a next-question response that arrives after reset', async () => {
    api.createQuiz.mockResolvedValue(quiz())
    api.answerQuestion.mockResolvedValue(answer)
    const pending = deferred<QuizDto>()
    api.getQuiz.mockReturnValue(pending.promise)

    const state = useQuiz()
    await state.createQuiz(payload)
    state.selectOption('OPTION_A')
    await state.submitAnswer()

    const next = state.nextQuestion()
    state.reset()
    pending.resolve(quiz(2))
    await next

    expect(state.phase.value).toBe('setup')
    expect(state.quiz.value).toBeNull()
  })

  it('rejects a next-question response that does not advance progress', async () => {
    api.createQuiz.mockResolvedValue(quiz())
    api.answerQuestion.mockResolvedValue(answer)
    api.getQuiz.mockResolvedValue(quiz(1))

    const state = useQuiz()
    await state.createQuiz(payload)
    state.selectOption('OPTION_A')
    await state.submitAnswer()
    await state.nextQuestion()

    expect(state.phase.value).toBe('feedback')
    expect(state.isLoadingNext.value).toBe(false)
    expect(state.errorMessage.value).toContain('invalid next question')
  })
})
