import { describe, it, expect } from 'vitest'
import { validateUsername, validatePassword } from './validation'

describe('validateUsername', () => {
  it('2文字は無効', () => {
    expect(validateUsername('ab')).toBeTruthy()
  })

  it('31文字は無効', () => {
    expect(validateUsername('a'.repeat(31))).toBeTruthy()
  })

  it('空文字は無効', () => {
    expect(validateUsername('')).toBeTruthy()
  })

  it('3文字は有効（下限境界）', () => {
    expect(validateUsername('abc')).toBeNull()
  })

  it('30文字は有効（上限境界）', () => {
    expect(validateUsername('a'.repeat(30))).toBeNull()
  })
})

describe('validatePassword', () => {
  it('7文字は無効', () => {
    expect(validatePassword('1234567')).toBeTruthy()
  })

  it('101文字は無効', () => {
    expect(validatePassword('a'.repeat(101))).toBeTruthy()
  })

  it('空文字は無効', () => {
    expect(validatePassword('')).toBeTruthy()
  })

  it('8文字は有効（下限境界）', () => {
    expect(validatePassword('12345678')).toBeNull()
  })

  it('100文字は有効（上限境界）', () => {
    expect(validatePassword('a'.repeat(100))).toBeNull()
  })
})
