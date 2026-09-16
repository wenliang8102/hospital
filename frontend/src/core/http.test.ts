import { describe, expect, it } from 'vitest'
import { http } from './http'

describe('http client', () => {
  it('uses the shared API defaults', () => {
    expect(http.defaults.baseURL).toBe('/api')
    expect(http.defaults.timeout).toBe(10_000)
  })
})

