<script setup lang="ts">
import { computed } from 'vue'
import { CheckCircle2, RotateCcw, Sparkles, XCircle } from 'lucide-vue-next'
import type { Option, ReviewItem } from '../types/quiz'
import { optionLabels } from '../types/quiz'

const props = defineProps<{
  items: ReviewItem[]
  topic: string
}>()

defineEmits<{
  (event: 'newQuiz'): void
}>()

const score = computed(() => props.items.filter((item) => item.isCorrect).length)
const percentage = computed(() => (props.items.length ? Math.round((score.value / props.items.length) * 100) : 0))

function optionText(item: ReviewItem, option: Option) {
  return `${optionLabels[option]} · ${item.options[option]}`
}
</script>

<template>
  <section class="review-shell" aria-labelledby="review-title">
    <div class="review-heading">
      <div>
        <span class="eyebrow">Session complete / {{ percentage }}% recall</span>
        <h1 id="review-title">You made it through <em>{{ topic }}.</em></h1>
      </div>
      <div class="score-stamp" aria-label="Final score">
        <span class="score-number">{{ score }}/{{ items.length }}</span>
        <span class="score-label">correct</span>
      </div>
    </div>

    <div class="review-toolbar">
      <p><strong>Full review</strong> · Keep the useful parts close.</p>
      <button class="ghost-button" type="button" @click="$emit('newQuiz')">
        <RotateCcw :size="16" />
        New quiz
      </button>
    </div>

    <div class="review-list">
      <article v-for="item in items" :key="item.number" class="review-card">
        <div class="review-card-top">
          <span class="review-number">PROMPT {{ String(item.number).padStart(2, '0') }}</span>
          <span class="review-result" :class="{ 'is-missed': !item.isCorrect }">
            <CheckCircle2 v-if="item.isCorrect" :size="14" />
            <XCircle v-else :size="14" />
            {{ item.isCorrect ? 'Correct' : 'Review' }}
          </span>
        </div>
        <h2>{{ item.question }}</h2>
        <p class="review-answer-line">
          Your answer: <strong>{{ optionText(item, item.selectedOption) }}</strong>
        </p>
        <p v-if="!item.isCorrect" class="review-answer-line">
          Correct answer: <strong>{{ optionText(item, item.correctOption) }}</strong>
        </p>
        <p class="review-explanation">{{ item.explanation }}</p>
      </article>
    </div>

    <div class="review-footer">
      <button class="primary-button" type="button" @click="$emit('newQuiz')">
        <Sparkles :size="17" />
        Forge another session
      </button>
    </div>
  </section>
</template>