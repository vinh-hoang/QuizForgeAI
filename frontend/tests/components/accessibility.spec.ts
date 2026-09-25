import { nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from '../../src/App.vue'
import QuizStage from '../../src/components/QuizStage.vue'
import type { QuizDto } from '../../src/types/quiz'

const quiz: QuizDto = {
  id: 'quiz-1',
  questionCount: 3,
  currentQuestionIndex: 1,
  currentQuestion: {
    question: 'Who founded Rome?',
    optionA: 'Romulus',
    optionB: 'Caesar',
    optionC: 'Cicero',
    optionD: 'Augustus',
    hint: 'The answer starts with R.',
  },
}

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

afterEach(() => {
  document.body.innerHTML = ''
  vi.unstubAllGlobals()
})

describe('frontend accessibility behavior', () => {
  it('announces hint disclosure state and labels answer controls', async () => {
    const wrapper = mount(QuizStage, {
      props: {
        answer: null,
        error: null,
        errorStatus: null,
        isLoadingNext: false,
        isSubmitting: false,
        quiz,
        selectedOption: null,
        topic: 'Roman history',
      },
    })

    const hintButton = wrapper.get('#hint-button')
    expect(hintButton.attributes('aria-expanded')).toBe('false')
    expect(hintButton.attributes('aria-controls')).toBe('question-hint')
    expect(wrapper.get('[data-screen-heading]').attributes('tabindex')).toBe('-1')
    expect(wrapper.get('[role="radio"]').text()).toContain('A')

    await hintButton.trigger('click')
    expect(hintButton.attributes('aria-expanded')).toBe('true')
    expect(wrapper.get('#question-hint').attributes('aria-labelledby')).toBe('hint-button')

    await wrapper.get('#option-OPTION_A').trigger('keydown', { key: 'ArrowRight' })
    expect(wrapper.emitted('select')?.at(-1)).toEqual(['OPTION_B'])
    wrapper.unmount()
  })

  it('moves initial focus to the setup screen heading', async () => {
    const wrapper = mount(App, { attachTo: document.body })
    await nextTick()

    expect(document.activeElement).toBe(wrapper.get('[data-screen-heading]').element)
    wrapper.unmount()
  })

  it('selects a rejected topic for replacement and allows a retry', async () => {
    const fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
    fetchMock
      .mockResolvedValueOnce(jsonResponse({ detail: 'The topic is not viable.' }, 422))
      .mockResolvedValueOnce(jsonResponse(quiz))

    const wrapper = mount(App, {
      attachTo: document.body,
      global: { stubs: { transition: false } },
    })
    const topicInput = wrapper.get('#topic')
    await topicInput.setValue('???')
    await wrapper.get('form').trigger('submit')

    await vi.waitFor(() => expect(wrapper.get('.form-error').text()).toContain('recognizable subject or activity'))
    const rejectedTopicInput = wrapper.get('#topic')
    await vi.waitFor(() => expect(document.activeElement).toBe(rejectedTopicInput.element))
    expect((rejectedTopicInput.element as HTMLInputElement).selectionStart).toBe(0)
    expect((rejectedTopicInput.element as HTMLInputElement).selectionEnd).toBe(3)
    expect((rejectedTopicInput.element as HTMLInputElement).value).toBe('???')

    await rejectedTopicInput.setValue('Roman history')
    await wrapper.get('form').trigger('submit')
    await vi.waitFor(() => expect(wrapper.get('.quiz-stage').exists()).toBe(true))
    expect(fetchMock).toHaveBeenCalledTimes(2)
    wrapper.unmount()
  })

  it('renders the conflict recovery action and emits reset', async () => {
    const wrapper = mount(QuizStage, {
      props: {
        answer: null,
        error: 'Quiz state conflict. Start a new quiz to recover.',
        errorStatus: 409,
        isLoadingNext: false,
        isSubmitting: false,
        quiz,
        selectedOption: null,
        topic: 'Roman history',
      },
    })

    const recovery = wrapper.get('.recovery-button')
    expect(recovery.text()).toBe('Start a new quiz')
    await recovery.trigger('click')
    expect(wrapper.emitted('reset')).toHaveLength(1)
    wrapper.unmount()
  })

  it('uses instant reset scrolling when reduced motion is requested', async () => {
    const scrollTo = vi.fn()
    vi.stubGlobal('matchMedia', vi.fn().mockReturnValue({ matches: true }))
    vi.stubGlobal('scrollTo', scrollTo)

    const wrapper = mount(App, { attachTo: document.body })
    await nextTick()
    await wrapper.get('.brand').trigger('click')

    expect(scrollTo).toHaveBeenCalledWith({ top: 0, behavior: 'auto' })
    wrapper.unmount()
  })

  it('moves focus to the reused question heading after advancing', async () => {
    const fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
    fetchMock
      .mockResolvedValueOnce(jsonResponse(quiz))
      .mockResolvedValueOnce(jsonResponse({ correctOption: 'OPTION_A', explanation: 'Romulus.' }))
      .mockResolvedValueOnce(jsonResponse({ ...quiz, currentQuestionIndex: 2 }))

    const wrapper = mount(App, { attachTo: document.body })
    await wrapper.get('#topic').setValue('Roman history')
    await wrapper.get('form').trigger('submit')
    await vi.waitFor(() => expect(wrapper.get('.quiz-stage').exists()).toBe(true))

    await wrapper.get('#option-OPTION_A').trigger('click')
    await wrapper.get('.answer-action').trigger('click')
    await vi.waitFor(() => expect(wrapper.get('.feedback-panel').exists()).toBe(true))

    await wrapper.get('.next-action').trigger('click')
    await vi.waitFor(() => expect(wrapper.get('.question-number').text()).toContain('02'))
    await nextTick()

    expect(document.activeElement).toBe(wrapper.get('[data-screen-heading]').element)
    wrapper.unmount()
  })
})
