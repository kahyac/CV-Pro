<template>
  <div class="row justify-content-center">
    <div class="col-md-5 col-lg-4">
      <div class="card p-4 shadow-sm">
        <h4 class="mb-1">Connexion</h4>
        <div class="text-muted small mb-3">
          Accède à ton espace CV
        </div>

        <!-- erreur -->
        <div
          v-if="error"
          class="alert alert-danger py-2 small d-flex align-items-center gap-2"
        >
          <i class="bi bi-x-circle"></i>
          <div class="flex-grow-1">{{ error }}</div>
          <button class="btn btn-sm btn-outline-light" @click="error=null">
            Fermer
          </button>
        </div>

        <form @submit.prevent="doLogin" class="vstack gap-2">
          <div>
            <label class="form-label small text-muted">Email</label>
            <input
              v-model="email"
              type="email"
              class="form-control"
              placeholder="ex: yacine.kartout@gmail.com"
              required
              autocomplete="email"
            >
          </div>

          <div>
            <label class="form-label small text-muted">Mot de passe</label>
            <input
              v-model="password"
              type="password"
              class="form-control"
              placeholder="••••••••"
              required
              autocomplete="current-password"
            >
          </div>

          <button class="btn btn-dark w-100 mt-2" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            Se connecter
          </button>

          <div class="text-center small mt-2">
            Pas de compte ?
            <RouterLink to="/register">Créer un compte</RouterLink>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const error = ref(null)
const loading = ref(false)

function normalizeAuthError(e) {
  const status = e?.response?.status
  const data = e?.response?.data

  if (status === 401) return "Session expirée ou token invalide."
  if (status === 403) return "Email ou mot de passe incorrect."
  if (data?.message) return data.message

  return "Erreur serveur, réessaie."
}

async function doLogin() {
  error.value = null
  loading.value = true
  try {
    const cleanEmail = email.value.trim().toLowerCase()
    const cleanPass = password.value.trim()

    await auth.login(cleanEmail, cleanPass)

    router.push('/cvs')
  } catch (e) {
    error.value = normalizeAuthError(e)
  } finally {
    loading.value = false
  }
}
</script>
