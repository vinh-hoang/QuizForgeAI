<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  text: string
}>()

interface TextSegment {
  text: string
  style: 'plain' | 'bold' | 'italic'
}

const segments = computed<TextSegment[]>(() => {
  const parsed: TextSegment[] = []
  let cursor = 0
  const markerPattern = /\*\*([^*]+)\*\*|\*([^*]+)\*/g
  let match: RegExpExecArray | null

  while ((match = markerPattern.exec(props.text)) !== null) {
    if (match.index > cursor) {
      parsed.push({ text: props.text.slice(cursor, match.index), style: 'plain' })
    }

    parsed.push({
      text: match[1] ?? match[2],
      style: match[1] === undefined ? 'italic' : 'bold',
    })
    cursor = markerPattern.lastIndex
  }

  if (cursor < props.text.length) {
    parsed.push({ text: props.text.slice(cursor), style: 'plain' })
  }

  return parsed
})
</script>

<template>
  <span class="markdown-text">
    <template v-for="(segment, index) in segments" :key="index">
      <strong v-if="segment.style === 'bold'">{{ segment.text }}</strong>
      <em v-else-if="segment.style === 'italic'">{{ segment.text }}</em>
      <template v-else>{{ segment.text }}</template>
    </template>
  </span>
</template>
