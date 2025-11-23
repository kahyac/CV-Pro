<template>
  <div>
    <div class="mb-3">
      <h3 class="mb-0">Modifier mon CV</h3>
      <div class="text-muted small">Ajoute, visualise ou supprime tes activités</div>
    </div>

    <!-- Pas connecté -->
    <div v-if="!auth.isAuthenticated" class="alert alert-warning d-flex align-items-center gap-2">
      <i class="bi bi-exclamation-triangle"></i>
      Tu dois être connecté pour modifier ton CV.
      <RouterLink to="/login" class="ms-auto btn btn-sm btn-outline-dark">Login</RouterLink>
    </div>

    <!-- Messages -->
    <div v-else class="mb-3">
      <div
        v-if="error"
        class="alert alert-danger py-2 small d-flex align-items-center gap-2"
      >
        <i class="bi bi-x-circle"></i>
        <div class="flex-grow-1">{{ error }}</div>
        <button class="btn btn-sm btn-outline-light" @click="error = null">
          Fermer
        </button>
      </div>

      <div
        v-if="success"
        class="alert alert-success py-2 small d-flex align-items-center gap-2"
      >
        <i class="bi bi-check-circle"></i>
        <div class="flex-grow-1">{{ success }}</div>
        <button class="btn btn-sm btn-outline-success" @click="success = null">
          OK
        </button>
      </div>
    </div>

    <!-- Connecté -->
    <div v-if="auth.isAuthenticated" class="row g-4">
      <!-- Form -->
      <div class="col-lg-5">
        <div class="card p-3">
          <h5 class="mb-3">Ajouter une activité</h5>

          <form @submit.prevent="addActivity" class="vstack gap-2">
            <div class="row g-2">
              <div class="col-4">
                <label class="form-label small text-muted">Année</label>
                <input
                  v-model.number="form.year"
                  type="number"
                  class="form-control"
                  required
                  min="1900"
                  max="3000"
                >
              </div>
              <div class="col-8">
                <label class="form-label small text-muted">Type</label>
                <select v-model="form.type" class="form-select" required>
                  <option value="EXPERIENCE">Expérience</option>
                  <option value="EDUCATION">Formation</option>
                  <option value="PROJECT">Projet</option>
                  <option value="OTHER">Autre</option>
                </select>
              </div>
            </div>

            <div>
              <label class="form-label small text-muted">Titre</label>
              <input
                v-model="form.title"
                class="form-control"
                placeholder="Ex: Développeur Java"
                required
                maxlength="200"
              >
            </div>

            <div>
              <label class="form-label small text-muted">Description</label>
              <textarea
                v-model="form.description"
                class="form-control"
                rows="3"
                placeholder="Décris brièvement..."
              />
            </div>

            <div>
              <label class="form-label small text-muted">Lien (optionnel)</label>
              <input
                v-model="form.url"
                class="form-control"
                placeholder="https://..."
              >
              <div v-if="urlError" class="text-danger small mt-1">
                {{ urlError }}
              </div>
            </div>

            <button class="btn btn-dark mt-2" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
              Ajouter
            </button>
          </form>
        </div>
      </div>

      <!-- List -->
      <div class="col-lg-7">
        <div class="card p-3">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <h5 class="mb-0">Mes activités</h5>
            <span class="badge text-bg-secondary">{{ activities.length }}</span>
          </div>

          <div v-if="loading" class="text-center p-4">
            <div class="spinner-border" role="status"></div>
            <div class="text-muted small mt-2">Chargement...</div>
          </div>

          <div v-else-if="!activities.length" class="text-center text-muted p-4">
            Aucune activité pour l’instant.
          </div>

          <ul v-else class="list-group">
            <li
              v-for="a in activities"
              :key="a.id"
              class="list-group-item d-flex justify-content-between align-items-start"
            >
              <div class="me-3">
                <div class="fw-semibold">{{ a.title }}</div>
                <div class="small text-muted">
                  {{ a.type }} • {{ a.year }}
                </div>
                <div class="small mt-1">{{ a.description }}</div>

                <a
                  v-if="a.url"
                  :href="a.url"
                  target="_blank"
                  rel="noopener"
                  class="small text-decoration-none d-inline-block mt-1"
                >
                  <i class="bi bi-link-45deg"></i> {{ a.url }}
                </a>
              </div>

              <button
                class="btn btn-sm btn-outline-danger"
                @click="deleteActivity(a.id)"
                :disabled="deletingId === a.id"
              >
                <span
                  v-if="deletingId === a.id"
                  class="spinner-border spinner-border-sm"
                />
                <i v-else class="bi bi-trash"></i>
              </button>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import api from '@/services/api'
import {useAuthStore} from '../stores/auth'

const auth = useAuthStore()
const personId = ref(null)

const activities = ref([])
const loading = ref(false)
const saving = ref(false)
const deletingId = ref(null)

const error = ref(null)
const success = ref(null)
const urlError = ref(null)

const form = ref({
  year: new Date().getFullYear(),
  type: 'PROJECT',
  title: '',
  description: '',
  url: ''
})

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

function normalizeBackendError(e) {
  // AxiosError standard
  const status = e?.response?.status
  const data = e?.response?.data

  if (status === 401) return "Session expirée. Reconnecte-toi."
  if (status === 403) return "Action interdite : tu ne peux modifier que ton propre CV."

  // erreurs validation Spring (MethodArgumentNotValidException)
  // souvent structure: { errors: [{field, message}]} ou {message:"..."}
  if (data?.errors?.length) {
    return data.errors.map(er => `${er.field}: ${er.message}`).join(" • ")
  }
  if (data?.message) return data.message

  return "Erreur serveur. Réessaie."
}

function validateUrl() {
  urlError.value = null
  const u = form.value.url?.trim()
  if (!u) return true
  if (!/^https?:\/\//i.test(u)) {
    urlError.value = "L’URL doit commencer par http:// ou https://"
    return false
  }
  return true
}

async function loadMyActivities() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get(`/persons/${personId.value}/activities?page=0&size=50`)
    activities.value = res.data.content
  } catch (e) {
    error.value = normalizeBackendError(e)
  } finally {
    loading.value = false
  }
}

async function addActivity() {
  error.value = null
  success.value = null

  if (!validateUrl()) return

  saving.value = true
  try {
    await api.post(`/persons/${personId.value}/activities`, {
      ...form.value,
      url: form.value.url?.trim() || null
    })

    success.value = "Activité ajoutée ✅"

    // reset form
    form.value.title = ''
    form.value.description = ''
    form.value.url = ''

    await loadMyActivities()
  } catch (e) {
    error.value = normalizeBackendError(e)
  } finally {
    saving.value = false
  }
}

async function deleteActivity(id) {
  error.value = null
  success.value = null
  deletingId.value = id

  try {
    await api.delete(`/activities/${id}`)
    success.value = "Activité supprimée ✅"
    await loadMyActivities()
  } catch (e) {
    error.value = normalizeBackendError(e)
  } finally {
    deletingId.value = null
  }
}

onMounted(async () => {
  if (!auth.token) return
  personId.value = decodeJwt(auth.token)
  if (!personId.value) {
    error.value = "Token invalide. Reconnecte-toi."
    return
  }
  await loadMyActivities()
})
</script>
