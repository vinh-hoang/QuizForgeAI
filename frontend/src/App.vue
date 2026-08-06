<script setup lang="ts">
import { ref } from 'vue'
import { BookOpen, Sparkles } from 'lucide-vue-next'
import LoadingState from './components/LoadingState.vue'
import QuizReview from './components/QuizReview.vue'
import QuizSetup from './components/QuizSetup.vue'
import QuizStage from './components/QuizStage.vue'
import { useQuiz } from './composables/useQuiz'
import type { Difficulty, NumberOfQuestions } from './types/quiz'

const topic = ref('')
const difficulty = ref<Difficulty>('BEGINNER')
const questionCount = ref<NumberOfQuestions>('FIVE')

const {
  answer,
  errorMessage,
  isLoadingNext,
  isSubmitting,
  phase,
  quiz,
  review,
  selectedOption,
  createQuiz,
  nextQuestion,
  reset,
  selectOption,
  submitAnswer,
} = useQuiz()

function handleCreate() {
  createQuiz({
    topic: topic.value.trim(),
    difficulty: difficulty.value,
    numberOfQuestions: questionCount.value,
  })
}

function handleNewQuiz() {
  reset()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <div class="app-shell">
    <div class="background-grid" aria-hidden="true"></div>

    <header class="app-header">
      <a class="brand" href="#" aria-label="QuizForge AI home" @click.prevent="handleNewQuiz">
        <span class="brand-mark"><Sparkles :size="17" stroke-width="2.5" /></span>
        <span class="brand-name">QuizForge <strong>AI</strong></span>
      </a>

      <div class="header-status">
        <span class="status-dot" aria-hidden="true"></span>
        <span>Study mode</span>
      </div>
    </header>

    <main class="app-main">
      <Transition name="screen" mode="out-in">
        <QuizSetup
          v-if="phase === 'setup'"
          :difficulty="difficulty"
          :error="errorMessage"
          :is-loading="false"
          :question-count="questionCount"
          :topic="topic"
          @create="handleCreate"
          @update:difficulty="difficulty = $event"
          @update:question-count="questionCount = $event"
          @update:topic="topic = $event"
        />

        <LoadingState v-else-if="phase === 'generating'" :topic="topic" />

        <QuizStage
          v-else-if="quiz && (phase === 'question' || phase === 'feedback')"
          :answer="answer"
          :error="errorMessage"
          :is-loading-next="isLoadingNext"
          :is-submitting="isSubmitting"
          :quiz="quiz"
          :selected-option="selectedOption"
          :topic="topic"
          @next="nextQuestion"
          @select="selectOption"
          @submit="submitAnswer"
        />

        <QuizReview
          v-else-if="phase === 'review'"
          :items="review"
          :topic="topic"
          @new-quiz="handleNewQuiz"
        />
      </Transition>
    </main>

    <footer class="app-footer">
      <span><BookOpen :size="15" /> Learn something worth remembering.</span>
      <span class="footer-rule" aria-hidden="true"></span>
      <span>Built for curious minds</span>
    </footer>
  </div>
</template>
