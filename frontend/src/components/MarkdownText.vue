<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  text: string
}>()

interface TextSegment {
  text: string
  isBold: boolean
}

const segments = computed<TextSegment[]>(() => {
  const parsed: TextSegment[] = []
  let cursor = 0

  while (cursor < props.text.length) {
    const openingMarker = props.text.indexOf('**', cursor)

    if (openingMarker === -1) {
      parsed.push({ text: props.text.slice(cursor), isBold: false })
      break
    }

    const closingMarker = props.text.indexOf('**', openingMarker + 2)

    if (closingMarker === -1) {
      parsed.push({ text: props.text.slice(cursor), isBold: false })
      break
    }

    if (openingMarker > cursor) {
      parsed.push({ text: props.text.slice(cursor, openingMarker), isBold: false })
    }

    parsed.push({
      text: props.text.slice(openingMarker + 2, closingMarker),
      isBold: true,
    })
    cursor = closingMarker + 2
  }

  return parsed
})
</script>

<template>
  <span class="markdown-text">
    <template v-for="(segment, index) in segments" :key="index">
      <strong v-if="segment.isBold">{{ segment.text }}</strong>
      <template v-else>{{ segment.text }}</template>
    </template>
  </span>
</template>
