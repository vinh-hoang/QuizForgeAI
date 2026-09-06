import type {
  AnswerResponse,
  CreateQuizRequest,
  Option,
  QuizDto,
} from '../types/quiz'
import { QUIZ_INDEX_BASE } from '../types/quiz'

export const DEFAULT_REQUEST_TIMEOUT_MS = 15_000

export type ApiErrorKind = 'http' | 'invalid-request' | 'invalid-response' | 'network' | 'timeout' | 'aborted'

export class ApiError extends Error {
  readonly kind: ApiErrorKind
  readonly status: number | null

  constructor(message: string, options: { kind: ApiErrorKind; status?: number | null; cause?: unknown }) {
    super(message, { cause: options.cause })
    this.name = 'ApiError'
    this.kind = options.kind
    this.status = options.status ?? null
  }
}

export interface QuizRequestOptions {
  signal?: AbortSignal
  timeoutMs?: number
}

type ResponseGuard<T> = (payload: unknown) => payload is T

const options: readonly Option[] = ['OPTION_A', 'OPTION_B', 'OPTION_C', 'OPTION_D']
const questionCounts = new Set([3, 5, 7])

function getApiBaseUrl(): string {
  return (import.meta.env.VITE_API_BASE_URL ?? '').trim().replace(/\/+$/, '')
}

export function buildApiUrl(path: string): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${getApiBaseUrl()}${normalizedPath}`
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isNonEmptyText(value: unknown): value is string {
  return typeof value === 'string' && value.trim().length > 0
}

function isQuestionDto(value: unknown): value is QuizDto['currentQuestion'] {
  return (
    isRecord(value) &&
    isNonEmptyText(value.question) &&
    isNonEmptyText(value.optionA) &&
    isNonEmptyText(value.optionB) &&
    isNonEmptyText(value.optionC) &&
    isNonEmptyText(value.optionD) &&
    typeof value.hint === 'string'
  )
}

export function isQuizDto(value: unknown): value is QuizDto {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    value.id.trim().length > 0 &&
    typeof value.questionCount === 'number' &&
    Number.isInteger(value.questionCount) &&
    questionCounts.has(value.questionCount) &&
    typeof value.currentQuestionIndex === 'number' &&
    Number.isInteger(value.currentQuestionIndex) &&
    value.currentQuestionIndex >= QUIZ_INDEX_BASE &&
    value.currentQuestionIndex <= value.questionCount &&
    isQuestionDto(value.currentQuestion)
  )
}

export function isAnswerResponse(value: unknown): value is AnswerResponse {
  return (
    isRecord(value) &&
    typeof value.correctOption === 'string' &&
    options.includes(value.correctOption as Option) &&
    isNonEmptyText(value.explanation)
  )
}

function isAbortError(error: unknown): boolean {
  return error instanceof Error && error.name === 'AbortError'
}

function normalizeTimeout(timeoutMs: number | undefined): number {
  return timeoutMs !== undefined && Number.isFinite(timeoutMs) && timeoutMs > 0
    ? timeoutMs
    : DEFAULT_REQUEST_TIMEOUT_MS
}

function encodePathSegment(value: string): string {
  try {
    return encodeURIComponent(value)
  } catch (error) {
    throw new ApiError('The quiz identifier is invalid.', {
      kind: 'invalid-request',
      cause: error,
    })
  }
}

function composeSignals(callerSignal: AbortSignal | undefined, timeoutMs: number) {
  const controller = new AbortController()
  let timedOut = false

  const abortFromCaller = () => controller.abort(callerSignal?.reason)
  if (callerSignal?.aborted) {
    abortFromCaller()
  } else {
    callerSignal?.addEventListener('abort', abortFromCaller, { once: true })
  }

  const timeoutId = setTimeout(() => {
    timedOut = true
    controller.abort()
  }, timeoutMs)

  return {
    signal: controller.signal,
    wasTimedOut: () => timedOut,
    cleanup: () => {
      clearTimeout(timeoutId)
      callerSignal?.removeEventListener('abort', abortFromCaller)
    },
  }
}

async function readErrorDetail(response: Response): Promise<string | null> {
  try {
    const payload: unknown = await response.json()
    if (!isRecord(payload)) {
      return null
    }

    const message = typeof payload.message === 'string' ? payload.message.trim() : ''
    const error = typeof payload.error === 'string' ? payload.error.trim() : ''
    const detail = typeof payload.detail === 'string' ? payload.detail.trim() : ''
    return message || error || detail || null
  } catch {
    return null
  }
}

function httpErrorMessage(status: number, detail: string | null): string {
  const prefix = status === 409 ? 'Quiz state conflict' : 'Request failed'
  const recovery = status === 409 ? ' Start a new quiz to recover.' : ''
  return `${prefix} (HTTP ${status})${detail ? `: ${detail}` : '.'}${recovery}`
}

async function request<T>(
  path: string,
  guard: ResponseGuard<T>,
  init: RequestInit = {},
  options: QuizRequestOptions = {},
): Promise<T> {
  const timeoutMs = normalizeTimeout(options.timeoutMs)
  const composed = composeSignals(options.signal, timeoutMs)
  const headers = new Headers(init.headers)

  if (init.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json')
  }

  try {
    const response = await fetch(buildApiUrl(path), {
      ...init,
      headers,
      signal: composed.signal,
    })

    if (!response.ok) {
      const detail = await readErrorDetail(response)
      throw new ApiError(httpErrorMessage(response.status, detail), {
        kind: 'http',
        status: response.status,
      })
    }

    let payload: unknown
    try {
      payload = await response.json()
    } catch (error) {
      throw new ApiError('The server returned an invalid JSON response.', {
        kind: 'invalid-response',
        cause: error,
      })
    }

    if (!guard(payload)) {
      throw new ApiError('The server returned an invalid quiz response.', {
        kind: 'invalid-response',
      })
    }

    return payload
  } catch (error) {
    if (composed.wasTimedOut()) {
      throw new ApiError(`Request timed out after ${timeoutMs} ms.`, {
        kind: 'timeout',
        cause: error,
      })
    }

    if (options.signal?.aborted || isAbortError(error)) {
      throw new ApiError('Request was cancelled.', { kind: 'aborted', cause: error })
    }

    if (error instanceof ApiError) {
      throw error
    }

    throw new ApiError('The network request failed.', { kind: 'network', cause: error })
  } finally {
    composed.cleanup()
  }
}

export function createQuiz(payload: CreateQuizRequest, options?: QuizRequestOptions): Promise<QuizDto> {
  return request('/quiz', isQuizDto, {
    method: 'POST',
    body: JSON.stringify(payload),
  }, options)
}

export function getQuiz(quizId: string, options?: QuizRequestOptions): Promise<QuizDto> {
  return request(`/quiz/${encodePathSegment(quizId)}`, isQuizDto, {
    cache: 'no-store',
  }, options)
}

export function answerQuestion(
  quizId: string,
  selectedOption: Option,
  options?: QuizRequestOptions,
): Promise<AnswerResponse> {
  return request(`/quiz/${encodePathSegment(quizId)}/question/current`, isAnswerResponse, {
    method: 'PATCH',
    body: JSON.stringify({ selectedOption }),
  }, options)
}
