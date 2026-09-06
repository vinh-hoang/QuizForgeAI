import { describe, expect, it } from 'vitest'
import viteConfig from '../../vite.config'

describe('Vite development configuration', () => {
  it('exposes the dev server for trusted LAN device testing', () => {
    expect(viteConfig.server?.host).toBe(true)
    expect(viteConfig.server?.proxy?.['/quiz']).toMatchObject({ target: 'http://localhost:8080' })
  })
})
