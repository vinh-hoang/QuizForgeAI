import type {
  AnswerResponse,
  CreateQuizRequest,
  Option,
  QuizDto,
} from '../types/quiz'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
  })

  if (!response.ok) {
    let message = `Request failed with status ${response.status}.`

    try {
      const payload = (await response.json()) as { message?: string; error?: string }
      message = payload.message ?? payload.error ?? message
    } catch {
      // Keep the status-based message when the server returns no JSON body.
    }

    throw new Error(message)
  }

  return response.json() as Promise<T>
}

export function createQuiz(payload: CreateQuizRequest): Promise<QuizDto> {
  return request<QuizDto>('/quiz', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getQuiz(quizId: string): Promise<QuizDto> {
  return request<QuizDto>(`/quiz/${quizId}`)
}

export function answerQuestion(quizId: string, selectedOption: Option): Promise<AnswerResponse> {
  return request<AnswerResponse>(`/quiz/${quizId}/question/current`, {
    method: 'PATCH',
    body: JSON.stringify({ selectedOption }),
  })
}