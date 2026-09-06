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

interface MathCandidate {
  end: number
  latex: string
  display: boolean
}

function isEscapedAt(text: string, index: number): boolean {
  let slashCount = 0
  for (let cursor = index - 1; cursor >= 0 && text[cursor] === '\\'; cursor -= 1) {
    slashCount += 1
  }
  return slashCount % 2 === 1
}

function findClosingDelimiter(text: string, delimiter: string, start: number): number {
  let cursor = text.indexOf(delimiter, start)
  while (cursor >= 0) {
    if (!isEscapedAt(text, cursor)) {
      return cursor
    }
    cursor = text.indexOf(delimiter, cursor + delimiter.length)
  }
  return -1
}

function hasInlineMathContent(latex: string): boolean {
  return latex.length > 0 && !/[\s\n]/.test(latex[0]) && !/[\s\n]/.test(latex[latex.length - 1])
}

function hasDisplayMathContent(latex: string): boolean {
  return latex.trim().length > 0
}

function findMathAt(text: string, index: number): MathCandidate | null {
  if (isEscapedAt(text, index)) {
    return null
  }

  if (text.startsWith('$$', index)) {
    const close = findClosingDelimiter(text, '$$', index + 2)
    if (close >= 0) {
      const latex = text.slice(index + 2, close)
      if (hasDisplayMathContent(latex)) {
        return { end: close + 2, latex, display: true }
      }
    }
    return null
  }

  const delimiters: Array<{ close: string; display: boolean; open: string }> = [
    { open: '\\[', close: '\\]', display: true },
    { open: '\\(', close: '\\)', display: false },
  ]

  for (const delimiter of delimiters) {
    if (!text.startsWith(delimiter.open, index)) {
      continue
    }

    const close = findClosingDelimiter(text, delimiter.close, index + delimiter.open.length)
    if (close < 0) {
      return null
    }

    const latex = text.slice(index + delimiter.open.length, close)
    if ((delimiter.display && hasDisplayMathContent(latex)) || (!delimiter.display && hasInlineMathContent(latex))) {
      return { end: close + delimiter.close.length, latex, display: delimiter.display }
    }
    return null
  }

  if (text[index] !== '$' || text[index + 1] === '$') {
    return null
  }

  const close = findClosingDelimiter(text, '$', index + 1)
  if (close < 0 || text[close + 1] === '$') {
    return null
  }

  const latex = text.slice(index + 1, close)
  const nextCharacter = text[close + 1]
  if (!hasInlineMathContent(latex) || (nextCharacter && /[A-Za-z0-9]/.test(nextCharacter))) {
    return null
  }

  return { end: close + 1, latex, display: false }
}

function isWordCharacter(character: string | undefined): boolean {
  return character !== undefined && /[A-Za-z0-9]/.test(character)
}

function isValidEmphasis(text: string, start: number, close: number, delimiterLength: number): boolean {
  const content = text.slice(start + delimiterLength, close)
  if (content.length === 0 || /[\s\n]/.test(content[0]) || /[\s\n]/.test(content[content.length - 1])) {
    return false
  }

  if (
    text[start - 1] === '*' ||
    text[start + delimiterLength] === '*' ||
    text[close - 1] === '*' ||
    text[close + delimiterLength] === '*'
  ) {
    return false
  }

  if (delimiterLength === 1 && isWordCharacter(text[start - 1]) && isWordCharacter(text[close + 1])) {
    return false
  }

  return true
}

function parseMarkdown(text: string): TextSegment[] {
  const parsed: TextSegment[] = []
  let cursor = 0
  let index = 0

  while (index < text.length) {
    if (isEscapedAt(text, index)) {
      index += 1
      continue
    }

    const delimiter = text.startsWith('**', index) ? '**' : text[index] === '*' ? '*' : null
    if (delimiter === null || (delimiter === '*' && text[index + 1] === '*')) {
      index += 1
      continue
    }

    const close = findClosingDelimiter(text, delimiter, index + delimiter.length)
    if (close < 0 || !isValidEmphasis(text, index, close, delimiter.length)) {
      index += delimiter.length
      continue
    }

    if (index > cursor) {
      parsed.push({ text: text.slice(cursor, index), style: 'plain' })
    }

    parsed.push({
      text: text.slice(index + delimiter.length, close),
      style: delimiter === '**' ? 'bold' : 'italic',
    })
    cursor = close + delimiter.length
    index = cursor
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

function renderMath(candidate: MathCandidate): string | null {
  try {
    return renderToString(normalizeLatex(candidate.latex.trim()), {
      displayMode: candidate.display,
      output: 'htmlAndMathml',
      throwOnError: false,
      trust: false,
    })
  } catch {
    return null
  }
}

function parseRichText(text: string): TextSegment[] {
  const parsed: TextSegment[] = []
  let cursor = 0
  let index = 0

  while (index < text.length) {
    const candidate = findMathAt(text, index)
    if (candidate === null) {
      index += text.startsWith('$$', index) ? 2 : 1
      continue
    }

    const html = renderMath(candidate)
    if (html === null) {
      index += 1
      continue
    }

    if (index > cursor) {
      parsed.push(...parseMarkdown(text.slice(cursor, index)))
    }

    parsed.push({ html, style: 'math', display: candidate.display })
    cursor = candidate.end
    index = cursor
  }

  if (cursor < text.length) {
    parsed.push(...parseMarkdown(text.slice(cursor)))
  }

  return parsed
}

const segments = computed(() => parseRichText(props.text))
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
