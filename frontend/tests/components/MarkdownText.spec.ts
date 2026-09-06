import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MarkdownText from '../../src/components/MarkdownText.vue'

describe('MarkdownText', () => {
  it('renders supported math without treating currency as math', () => {
    const wrapper = mount(MarkdownText, {
      props: { text: 'Solve $x^2$; the price is $5 and $10.' },
    })

    expect(wrapper.findAll('.markdown-math')).toHaveLength(1)
    expect(wrapper.text()).toContain('the price is $5 and $10.')
  })

  it('supports display math and preserves operator-like asterisks', () => {
    const wrapper = mount(MarkdownText, {
      props: { text: '$$\\frac{a}{b}$$ and a * b * c' },
    })

    expect(wrapper.find('.markdown-math--display').exists()).toBe(true)
    expect(wrapper.find('em').exists()).toBe(false)
    expect(wrapper.text()).toContain('a * b * c')
  })

  it('keeps valid emphasis while ignoring malformed markers', () => {
    const wrapper = mount(MarkdownText, {
      props: { text: '**Useful** and *clear* but * unfinished' },
    })

    expect(wrapper.find('strong').text()).toBe('Useful')
    expect(wrapper.find('em').text()).toBe('clear')
    expect(wrapper.text()).toContain('* unfinished')
  })

  it('keeps escaped and malformed math delimiters literal', () => {
    const wrapper = mount(MarkdownText, {
      props: { text: String.raw`\$x\$ and $ x $ and ***bold***` },
    })

    expect(wrapper.findAll('.markdown-math')).toHaveLength(0)
    expect(wrapper.text()).toContain(String.raw`\$x\$ and $ x $ and ***bold***`)
    expect(wrapper.find('strong').exists()).toBe(false)
    expect(wrapper.find('em').exists()).toBe(false)
  })

  it('does not emit unsafe HTML from literal content', () => {
    const wrapper = mount(MarkdownText, {
      props: { text: '<script>alert(1)</script> $\\htmlClass{danger}{x}$' },
    })

    expect(wrapper.html()).not.toContain('<script>')
    expect(wrapper.html()).not.toContain('class="danger"')
  })
})
