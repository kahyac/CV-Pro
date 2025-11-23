import { defineStore } from 'pinia'
import api from '@/services/api'

function decodeJwt(token) {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64).split('').map(c =>
        '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      ).join('')
    )
    return JSON.parse(jsonPayload).sub
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token'),
    me: null,
  }),
  getters: {
    isAuthenticated: (s) => !!s.token,
  },
  actions: {
    async fetchMe() {
      if (!this.token) {
        this.me = null
        return
      }
      const id = decodeJwt(this.token)
      if (!id) {
        this.logout()
        return
      }
      const res = await api.get(`/persons/${id}`)
      this.me = res.data
    },

    async login(email, password) {
      try {
        const res = await api.post('/auth/login', { email, password })
        this.token = res.data.token
        localStorage.setItem('token', this.token)

        await this.fetchMe()
      } catch (err) {
        this.logout()
        throw err
      }
    },

    logout() {
      this.token = null
      this.me = null
      localStorage.removeItem('token')
    },
  },
})
