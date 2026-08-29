<script setup lang="ts">
import { computed } from 'vue'
import { renderToString } from 'katex'

const props = defineProps<{
  text: string
}>()

type TextSegment =
  | {
      text: string
      style: 'plain' | 'bold' | 'italic'
    }
  | {
      html: string
      style: 'math'
      display: boolean
    }

function parseMarkdown(text: string): TextSegment[] {
  const parsed: TextSegment[] = []
  let cursor = 0
  const markerPattern = /\*\*([^*]+)\*\*|\*([^*]+)\*/g
  let match: RegExpExecArray | null

  while ((match = markerPattern.exec(text)) !== null) {
    if (match.index > cursor) {
      parsed.push({ text: text.slice(cursor, match.index), style: 'plain' })
    }

    parsed.push({
      text: match[1] ?? match[2],
      style: match[1] === undefined ? 'italic' : 'bold',
    })
    cursor = markerPattern.lastIndex
  }

  if (cursor < text.length) {
    parsed.push({ text: text.slice(cursor), style: 'plain' })
  }

  return parsed
}

function normalizeLatex(latex: string): string {
  const restoredCommands = latex
    .replace(/\u0008/g, '\\b')
    .replace(/\u000c/g, '\\f')
    .replace(/\t/g, '\\t')

  return restoredCommands.replace(
    /\\begin\{([A-Za-z]+matrix)\}([\s\S]*?)(?:\\end\{\1\}|\\times\s*\\text\{\1\})/g,
    (_match, environment: string, body: string) => {
      const rows = body.replace(/(?<!\\)\\(?=\s)/g, '\\\\')
      return `\\begin{${environment}}${rows}\\end{${environment}}`
    },
  )
}

const segments = computed<TextSegment[]>(() => {
  const parsed: TextSegment[] = []
  let cursor = 0
  let match: RegExpExecArray | null
  const mathPattern = /\$\$([\s\S]*?)\$\$|\\\[([\s\S]*?)\\\]|\\\(([\s\S]*?)\\\)|\$((?:\\.|[^$\\])*)\$/g

  while ((match = mathPattern.exec(props.text)) !== null) {
    const isEscaped = match.index > 0 && props.text[match.index - 1] === '\\'
    if (isEscaped) {
      continue
    }

    if (match.index > cursor) {
      parsed.push(...parseMarkdown(props.text.slice(cursor, match.index)))
    }

    const latex = match[1] ?? match[2] ?? match[3] ?? match[4]
    const display = match[1] !== undefined || match[2] !== undefined

    parsed.push({
      html: renderToString(normalizeLatex(latex.trim()), {
        displayMode: display,
        output: 'htmlAndMathml',
        throwOnError: false,
        trust: false,
      }),
      style: 'math',
      display,
    })
    cursor = mathPattern.lastIndex
  }

  if (cursor < props.text.length) {
    parsed.push(...parseMarkdown(props.text.slice(cursor)))
  }

  return parsed
})
</script>

<template>
  <span class="markdown-text">
    <template v-for="(segment, index) in segments" :key="index">
      <span
        v-if="segment.style === 'math'"
        class="markdown-math"
        :class="{ 'markdown-math--display': segment.display }"
        v-html="segment.html"
      ></span>
      <strong v-else-if="segment.style === 'bold'">{{ segment.text }}</strong>
      <em v-else-if="segment.style === 'italic'">{{ segment.text }}</em>
      <template v-else>{{ segment.text }}</template>
    </template>
  </span>
</template>
