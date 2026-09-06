export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD'

export type NumberOfQuestions = 'THREE' | 'FIVE' | 'SEVEN'

export type Option = 'OPTION_A' | 'OPTION_B' | 'OPTION_C' | 'OPTION_D'

/** The backend mapper exposes the current question number as one-based. */
export const QUIZ_INDEX_BASE = 1 as const

export interface CreateQuizRequest {
  topic: string
  numberOfQuestions: NumberOfQuestions
  difficulty: Difficulty
}

export interface QuestionDto {
  question: string
  optionA: string
  optionB: string
  optionC: string
  optionD: string
  hint: string
}

export interface QuizDto {
  id: string
  questionCount: number
  /** One-based question number, matching the current backend mapper contract. */
  currentQuestionIndex: number
  currentQuestion: QuestionDto
}

export interface AnswerResponse {
  correctOption: Option
  explanation: string
}

export interface ReviewItem {
  number: number
  question: string
  options: Record<Option, string>
  selectedOption: Option
  correctOption: Option
  explanation: string
  isCorrect: boolean
}

export const optionLabels: Record<Option, string> = {
  OPTION_A: 'A',
  OPTION_B: 'B',
  OPTION_C: 'C',
  OPTION_D: 'D',
}
