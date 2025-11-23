import { defineStore } from 'pinia'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token'),
    me: null,
  }),
  getters: {
    isAuthenticated: (s) => !!s.token,
  },
  actions: {
    async login(email, password) {
      try {
        const res = await api.post('/auth/login', { email, password })
        this.token = res.data.token
        localStorage.setItem('token', this.token)
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
