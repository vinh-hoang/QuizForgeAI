<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  ArrowRight,
  BookOpen,
  Check,
  CheckCircle2,
  ChevronRight,
  Lightbulb,
  LoaderCircle,
  X,
  XCircle,
} from 'lucide-vue-next'
import type { AnswerResponse, Option, QuizDto } from '../types/quiz'
import { optionLabels } from '../types/quiz'

const props = defineProps<{
  answer: AnswerResponse | null
  error: string | null
  isLoadingNext: boolean
  isSubmitting: boolean
  quiz: QuizDto
  selectedOption: Option | null
  topic: string
}>()

const emit = defineEmits<{
  (event: 'select', option: Option): void
  (event: 'submit'): void
  (event: 'next'): void
}>()

const showHint = ref(false)

const options = computed<Array<{ key: Option; label: string; text: string }>>(() => [
  { key: 'OPTION_A', label: optionLabels.OPTION_A, text: props.quiz.currentQuestion.optionA },
  { key: 'OPTION_B', label: optionLabels.OPTION_B, text: props.quiz.currentQuestion.optionB },
  { key: 'OPTION_C', label: optionLabels.OPTION_C, text: props.quiz.currentQuestion.optionC },
  { key: 'OPTION_D', label: optionLabels.OPTION_D, text: props.quiz.currentQuestion.optionD },
])

const progress = computed(() => Math.min(100, (props.quiz.currentQuestionIndex / props.quiz.questionCount) * 100))
const isCorrect = computed(() => props.answer !== null && props.selectedOption === props.answer.correctOption)
const isLastQuestion = computed(() => props.quiz.currentQuestionIndex >= props.quiz.questionCount)

watch(
  () => props.quiz.currentQuestionIndex,
  () => {
    showHint.value = false
  },
)
</script>

<template>
  <section class="quiz-stage" aria-labelledby="question-title">
    <div class="quiz-topbar">
      <span class="topic-label"><BookOpen :size="15" /> {{ topic }}</span>
      <div>
        <div class="progress-readout">Question {{ quiz.currentQuestionIndex }} <span>/ {{ quiz.questionCount }}</span></div>
        <div class="progress-track" aria-hidden="true">
          <div class="progress-value" :style="{ width: `${progress}%` }"></div>
        </div>
      </div>
    </div>

    <div class="question-layout">
      <div class="question-copy">
        <span class="question-number">Prompt {{ String(quiz.currentQuestionIndex).padStart(2, '0') }}</span>
        <h1 id="question-title">{{ quiz.currentQuestion.question }}</h1>
        <button class="hint-button" type="button" @click="showHint = !showHint">
          <Lightbulb :size="16" />
          <span>{{ showHint ? 'Hide hint' : 'Show hint' }}</span>
          <ChevronRight :size="14" :class="{ 'hint-arrow-open': showHint }" />
        </button>
        <div v-if="showHint" class="hint-panel">
          {{ quiz.currentQuestion.hint }}
        </div>
      </div>

      <div class="answer-panel">
        <div class="option-list" role="radiogroup" aria-label="Answer options">
          <button
            v-for="option in options"
            :key="option.key"
            class="option-button"
            :class="{
              'is-selected': selectedOption === option.key,
              'is-correct': answer && option.key === answer.correctOption,
              'is-incorrect': answer && selectedOption === option.key && option.key !== answer.correctOption,
            }"
            :aria-checked="selectedOption === option.key"
            :disabled="answer !== null || isSubmitting"
            role="radio"
            type="button"
            @click="emit('select', option.key)"
          >
            <span class="option-letter">{{ option.label }}</span>
            <span class="option-text">{{ option.text }}</span>
            <span class="option-state" aria-hidden="true">
              <Check v-if="answer && option.key === answer.correctOption" :size="17" />
              <X v-else-if="answer && selectedOption === option.key" :size="17" />
            </span>
          </button>
        </div>

        <button
          v-if="!answer"
          class="primary-button answer-action"
          :disabled="!selectedOption || isSubmitting"
          type="button"
          @click="emit('submit')"
        >
          <LoaderCircle v-if="isSubmitting" class="button-spinner" :size="17" />
          <span>{{ isSubmitting ? 'Checking your answer' : 'Lock in answer' }}</span>
          <ArrowRight v-if="!isSubmitting" :size="17" />
        </button>

        <div
          v-if="answer"
          class="feedback-panel"
          :class="isCorrect ? 'feedback-panel--correct' : 'feedback-panel--review'"
          role="status"
        >
          <div class="feedback-heading">
            <CheckCircle2 v-if="isCorrect" :size="22" />
            <XCircle v-else :size="22" />
            <div>
              <span class="feedback-kicker">{{ isCorrect ? 'Correct answer' : 'Keep learning' }}</span>
              <h3>{{ isCorrect ? 'That one landed.' : 'A useful miss.' }}</h3>
            </div>
          </div>
          <p class="feedback-explanation">{{ answer.explanation }}</p>
          <p class="correct-answer">
            Correct option:
            <strong>{{ optionLabels[answer.correctOption] }}</strong>
          </p>
        </div>

        <button v-if="answer" class="primary-button next-action" :disabled="isLoadingNext" type="button" @click="emit('next')">
          <LoaderCircle v-if="isLoadingNext" class="button-spinner" :size="17" />
          <span>{{ isLoadingNext ? 'Loading next question' : isLastQuestion ? 'See full review' : 'Next question' }}</span>
          <ArrowRight v-if="!isLoadingNext" :size="17" />
        </button>

        <p v-if="error" class="error-message stage-error" role="alert">
          <span aria-hidden="true">!</span>
          {{ error }}
        </p>
      </div>
    </div>
  </section>
</template>