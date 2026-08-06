export type Difficulty = 'BEGINNER' | 'ADVANCED' | 'EXPERT'

export type NumberOfQuestions = 'FIVE' | 'TEN' | 'FIFTEEN'

export type Option = 'OPTION_A' | 'OPTION_B' | 'OPTION_C' | 'OPTION_D'

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
  /** The current backend mapper sends this as a one-based question number. */
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