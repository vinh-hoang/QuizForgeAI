<script setup lang="ts">
import type { Component } from 'vue'
import {
  ArrowRight,
  BookOpen,
  Flame,
  Gauge,
  Layers3,
  LoaderCircle,
  Sprout,
} from 'lucide-vue-next'
import type { Difficulty, NumberOfQuestions } from '../types/quiz'

defineProps<{
  topic: string
  difficulty: Difficulty
  questionCount: NumberOfQuestions
  error: string | null
  isLoading: boolean
}>()

const emit = defineEmits<{
  (event: 'update:topic', value: string): void
  (event: 'update:difficulty', value: Difficulty): void
  (event: 'update:questionCount', value: NumberOfQuestions): void
  (event: 'create'): void
}>()

const difficultyOptions: Array<{
  value: Difficulty
  label: string
  detail: string
  icon: Component
}> = [
  { value: 'BEGINNER', label: 'Beginner', detail: 'Warm up', icon: Sprout },
  { value: 'ADVANCED', label: 'Advanced', detail: 'Stretch out', icon: Gauge },
  { value: 'EXPERT', label: 'Expert', detail: 'Go deep', icon: Flame },
]

const questionOptions: Array<{ value: NumberOfQuestions; label: string }> = [
  { value: 'FIVE', label: '5 questions' },
  { value: 'TEN', label: '10 questions' },
  { value: 'FIFTEEN', label: '15 questions' },
]

function handleTopicInput(event: Event) {
  emit('update:topic', (event.target as HTMLInputElement).value)
}
</script>

<template>
  <section class="setup-layout" aria-labelledby="setup-title">
    <div class="setup-copy">
      <span class="eyebrow">AI-powered practice lab</span>
      <h1 id="setup-title">Turn curiosity into <span>momentum.</span></h1>
      <p>
        Shape a quick study session around anything you want to understand. Each question is a small step forward.
      </p>
      <div class="setup-note">
        <Layers3 :size="16" />
        <span>One topic. Fresh questions. Better recall.</span>
      </div>
    </div>

    <form class="setup-form" @submit.prevent="emit('create')">
      <div class="form-heading">
        <div>
          <span class="section-kicker">01 / Set the focus</span>
          <h2>What should we explore?</h2>
        </div>
        <span class="form-index">QUIZ / 001</span>
      </div>

      <div class="field-group">
        <label class="field-label" for="topic">Topic</label>
        <div class="input-shell">
          <BookOpen :size="18" />
          <input
            id="topic"
            :value="topic"
            maxlength="80"
            placeholder="e.g. The Roman Republic"
            required
            type="text"
            @input="handleTopicInput"
          />
          <span class="char-count">{{ topic.length }}/80</span>
        </div>
      </div>

      <fieldset class="field-group">
        <legend>Difficulty</legend>
        <div class="choice-grid">
          <label
            v-for="option in difficultyOptions"
            :key="option.value"
            class="choice-card"
            :class="{ selected: difficulty === option.value }"
          >
            <input
              :checked="difficulty === option.value"
              name="difficulty"
              type="radio"
              :value="option.value"
              @change="emit('update:difficulty', option.value)"
            />
            <span class="choice-icon"><component :is="option.icon" :size="19" /></span>
            <span class="choice-title">{{ option.label }}</span>
            <span class="choice-detail">{{ option.detail }}</span>
          </label>
        </div>
      </fieldset>

      <fieldset class="field-group">
        <legend>Session length</legend>
        <div class="count-grid">
          <button
            v-for="option in questionOptions"
            :key="option.value"
            class="count-option"
            :class="{ selected: questionCount === option.value }"
            :aria-pressed="questionCount === option.value"
            type="button"
            @click="emit('update:questionCount', option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </fieldset>

      <p v-if="error" class="error-message form-error" role="alert">
        <span aria-hidden="true">!</span>
        {{ error }}
      </p>

      <button class="primary-button setup-submit" :disabled="isLoading || !topic.trim()" type="submit">
        <LoaderCircle v-if="isLoading" class="button-spinner" :size="17" />
        <span>{{ isLoading ? 'Building your quiz' : 'Forge my quiz' }}</span>
        <ArrowRight v-if="!isLoading" :size="17" />
      </button>
    </form>
  </section>
</template>