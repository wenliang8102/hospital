import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { http, type ApiResponse } from '@/core/http'
import { clearStoredAuth, loadStoredAuth, saveStoredAuth } from '@/core/auth-storage'

export interface UserProfile {
  id: number
  username: string
  displayName: string
  employeeId: number | null
  roles: string[]
  permissions: string[]
}

interface LoginResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: UserProfile
}

export const useAuthStore = defineStore('auth', () => {
  const stored = loadStoredAuth()
  const token = ref(stored?.token ?? '')
  const user = ref<UserProfile | null>(stored?.user ?? null)
  const isAuthenticated = computed(() => Boolean(token.value && user.value))

  async function login(username: string, password: string) {
    const response = await http.post<ApiResponse<LoginResponse>>('/auth/login', { username, password })
    token.value = response.data.data.accessToken
    user.value = response.data.data.user
    saveStoredAuth({ token: token.value, user: user.value })
  }

  function logout() {
    token.value = ''
    user.value = null
    clearStoredAuth()
  }

  function hasPermission(permission: string) {
    return user.value?.permissions.includes(permission) ?? false
  }

  function hasAnyPermission(permissions: string[]) {
    return permissions.some(hasPermission)
  }

  window.addEventListener('his:unauthorized', logout)

  return { token, user, isAuthenticated, login, logout, hasPermission, hasAnyPermission }
})
