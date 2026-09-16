import type { UserProfile } from '@/stores/auth'

const STORAGE_KEY = 'hospital-his.auth'

export interface StoredAuth {
  token: string
  user: UserProfile
}

export function loadStoredAuth(): StoredAuth | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    return value ? JSON.parse(value) as StoredAuth : null
  } catch {
    return null
  }
}

export function saveStoredAuth(auth: StoredAuth) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(auth))
}

export function clearStoredAuth() {
  localStorage.removeItem(STORAGE_KEY)
}

export function readAccessToken(): string {
  return loadStoredAuth()?.token ?? ''
}
