import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, refreshToken, logout as logoutApi, getCurrentUser } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshTokenValue = ref(localStorage.getItem('refreshToken') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(credentials) {
    const res = await loginApi(credentials)
    const { accessToken, refreshToken: refresh, user: userInfo } = res.data
    token.value = accessToken
    refreshTokenValue.value = refresh
    user.value = userInfo
    localStorage.setItem('token', accessToken)
    localStorage.setItem('refreshToken', refresh)
    localStorage.setItem('user', JSON.stringify(userInfo))
    return res
  }

  async function refresh() {
    try {
      const res = await refreshToken(refreshTokenValue.value)
      const { accessToken } = res.data
      token.value = accessToken
      localStorage.setItem('token', accessToken)
    } catch {
      logout()
    }
  }

  async function fetchUser() {
    try {
      const res = await getCurrentUser()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
    } catch {
      // ignore
    }
  }

  function logout() {
    try { logoutApi() } catch { /* ignore */ }
    token.value = ''
    refreshTokenValue.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  return { token, refreshTokenValue, user, isLoggedIn, isAdmin, login, refresh, fetchUser, logout }
})
