import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  answerQuestion,
  ApiError,
  buildApiUrl,
  createQuiz,
  DEFAULT_REQUEST_TIMEOUT_MS,
  getQuiz,
  QUIZ_CREATION_TIMEOUT_MS,
} from '../../src/api/quizApi'
import type { CreateQuizRequest, QuizDto } from '../../src/types/quiz'

const request: CreateQuizRequest = {
  topic: 'The Roman Republic',
  numberOfQuestions: 'THREE',
  difficulty: 'EASY',
}

const quiz: QuizDto = {
  id: 'quiz-1',
  questionCount: 3,
  currentQuestionIndex: 1,
  currentQuestion: {
    question: 'Who founded Rome?',
    optionA: 'Romulus',
    optionB: 'Caesar',
    optionC: 'Cicero',
    optionD: 'Augustus',
    hint: 'The name starts with R.',
  },
}

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('quizApi', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.unstubAllEnvs()
    vi.useRealTimers()
  })

  it('validates a quiz response and sends JSON to the relative API path', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock.mockResolvedValue(jsonResponse(quiz))

    await expect(createQuiz(request)).resolves.toEqual(quiz)

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/quiz')
    expect(init?.method).toBe('POST')
    expect(init?.headers).toBeInstanceOf(Headers)
    expect(JSON.parse(String(init?.body))).toEqual(request)
    expect(init?.signal).toBeInstanceOf(AbortSignal)
  })

  it('uses no-store for quiz reads and supports a configured API origin', async () => {
    vi.stubEnv('VITE_API_BASE_URL', 'https://quiz-api.example.test/')
    const fetchMock = vi.mocked(fetch)
    fetchMock.mockResolvedValue(jsonResponse(quiz))

    expect(buildApiUrl('/quiz/quiz-1')).toBe('https://quiz-api.example.test/quiz/quiz-1')
    await getQuiz('quiz-1')

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('https://quiz-api.example.test/quiz/quiz-1')
    expect(init?.cache).toBe('no-store')
    expect((init?.headers as Headers).has('Content-Type')).toBe(false)
  })

  it.each([
    ['null', null],
    ['missing current question', { ...quiz, currentQuestion: undefined }],
    ['wrong question count type', { ...quiz, questionCount: '3' }],
    ['zero-based current index', { ...quiz, currentQuestionIndex: 0 }],
    ['current index past completion', { ...quiz, currentQuestionIndex: 4 }],
    ['empty question', { ...quiz, currentQuestion: { ...quiz.currentQuestion, question: ' ' } }],
    ['empty answer option', { ...quiz, currentQuestion: { ...quiz.currentQuestion, optionA: '' } }],
  ])('rejects %s success payloads instead of trusting the status code', async (_label, payload) => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse(payload))

    await expect(createQuiz(request)).rejects.toMatchObject({
      kind: 'invalid-response',
      status: null,
    })
  })

  it('preserves HTTP status and gives 409 a recovery message', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ message: 'Already answered' }, 409))

    await expect(answerQuestion('quiz-1', 'OPTION_A')).rejects.toEqual(
      expect.objectContaining({
        kind: 'http',
        status: 409,
        message: expect.stringContaining('HTTP 409'),
      }),
    )
    await expect(answerQuestion('quiz-1', 'OPTION_A')).rejects.toThrow('Start a new quiz to recover')
  })

  it('reads Spring Problem Details from an HTTP error', async () => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse({ detail: 'The quiz is already complete.' }, 409))

    await expect(answerQuestion('quiz-1', 'OPTION_A')).rejects.toThrow('The quiz is already complete.')
  })

  it('rejects invalid path identifiers as typed request errors', () => {
    expect(() => getQuiz('\uD800')).toThrow(ApiError)
    expect(() => getQuiz('\uD800')).toThrow('The quiz identifier is invalid.')
  })

  it('reports malformed JSON as a typed API error', async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response('{not-json', { status: 200, headers: { 'Content-Type': 'application/json' } }),
    )

    await expect(createQuiz(request)).rejects.toMatchObject({
      kind: 'invalid-response',
      message: 'The server returned an invalid JSON response.',
    })
  })

  it.each([
    ['null', null],
    ['unknown option', { correctOption: 'OPTION_E', explanation: 'Nope.' }],
    ['missing explanation', { correctOption: 'OPTION_A' }],
  ])('rejects %s answer payloads', async (_label, payload) => {
    vi.mocked(fetch).mockResolvedValue(jsonResponse(payload))

    await expect(answerQuestion('quiz-1', 'OPTION_A')).rejects.toMatchObject({
      kind: 'invalid-response',
      status: null,
    })
  })

  it('turns a bounded timeout into an actionable error', async () => {
    vi.useFakeTimers()
    vi.mocked(fetch).mockImplementation(
      (_input, init) =>
        new Promise((_resolve, reject) => {
          init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')))
        }),
    )

    const pending = answerQuestion('quiz-1', 'OPTION_A', { timeoutMs: 1000 })
    const rejection = expect(pending).rejects.toMatchObject({
      kind: 'timeout',
      message: 'Request timed out after 1000 ms.',
    })
    await vi.advanceTimersByTimeAsync(1000)

    await rejection
  })

  it('allows quiz creation to finish after the former 15-second default', async () => {
    vi.useFakeTimers()
    vi.mocked(fetch).mockImplementation(
      (_input, init) =>
        new Promise((resolve, reject) => {
          const timer = setTimeout(() => resolve(jsonResponse(quiz)), DEFAULT_REQUEST_TIMEOUT_MS + 1)
          init?.signal?.addEventListener('abort', () => {
            clearTimeout(timer)
            reject(new DOMException('Aborted', 'AbortError'))
          }, { once: true })
        }),
    )

    let settled = false
    const pending = createQuiz(request).finally(() => {
      settled = true
    })

    await vi.advanceTimersByTimeAsync(DEFAULT_REQUEST_TIMEOUT_MS)
    expect(settled).toBe(false)

    await vi.advanceTimersByTimeAsync(1)
    await expect(pending).resolves.toEqual(quiz)
    expect(QUIZ_CREATION_TIMEOUT_MS).toBe(300_000)
  })

  it('keeps the five-minute minimum when a creation timeout is too short', async () => {
    vi.useFakeTimers()
    vi.mocked(fetch).mockImplementation(
      (_input, init) =>
        new Promise((_resolve, reject) => {
          init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')))
        }),
    )

    const pending = createQuiz(request, { timeoutMs: 1000 })
    const rejection = expect(pending).rejects.toMatchObject({
      kind: 'timeout',
      message: `Request timed out after ${QUIZ_CREATION_TIMEOUT_MS} ms.`,
    })

    await vi.advanceTimersByTimeAsync(DEFAULT_REQUEST_TIMEOUT_MS)
    await vi.advanceTimersByTimeAsync(QUIZ_CREATION_TIMEOUT_MS - DEFAULT_REQUEST_TIMEOUT_MS)

    await rejection
  })

  it.each([-1, Number.NaN, Number.POSITIVE_INFINITY])('falls back to the default timeout for %s', async (timeoutMs) => {
    vi.useFakeTimers()
    vi.mocked(fetch).mockImplementation(
      (_input, init) =>
        new Promise((_resolve, reject) => {
          init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')))
        }),
    )

    const pending = answerQuestion('quiz-1', 'OPTION_A', { timeoutMs })
    const rejection = expect(pending).rejects.toMatchObject({ kind: 'timeout' })
    await vi.advanceTimersByTimeAsync(DEFAULT_REQUEST_TIMEOUT_MS)

    await rejection
  })

  it('keeps the shorter default timeout for quiz reads and answers', async () => {
    vi.useFakeTimers()
    vi.mocked(fetch).mockImplementation(
      (_input, init) =>
        new Promise((_resolve, reject) => {
          init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')))
        }),
    )

    const getRejection = expect(getQuiz('quiz-1')).rejects.toMatchObject({
      kind: 'timeout',
      message: `Request timed out after ${DEFAULT_REQUEST_TIMEOUT_MS} ms.`,
    })
    const answerRejection = expect(answerQuestion('quiz-1', 'OPTION_A')).rejects.toMatchObject({
      kind: 'timeout',
      message: `Request timed out after ${DEFAULT_REQUEST_TIMEOUT_MS} ms.`,
    })

    await vi.advanceTimersByTimeAsync(DEFAULT_REQUEST_TIMEOUT_MS)

    await Promise.all([getRejection, answerRejection])
  })

  it('composes caller cancellation and reports intentional abort separately from timeout', async () => {
    const controller = new AbortController()
    vi.mocked(fetch).mockImplementation(
      (_input, init) =>
        new Promise((_resolve, reject) => {
          init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')))
        }),
    )

    const pending = createQuiz(request, { signal: controller.signal })
    controller.abort()

    await expect(pending).rejects.toMatchObject({ kind: 'aborted' })
    expect(pending).toBeInstanceOf(Promise)
  })

  it('exposes ApiError as an Error with a stable status field', () => {
    const error = new ApiError('Conflict', { kind: 'http', status: 409 })

    expect(error).toBeInstanceOf(Error)
    expect(error.status).toBe(409)
    expect(error.kind).toBe('http')
  })
})
