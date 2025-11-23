<template>
  <div class="row justify-content-center">
    <div class="col-md-8 col-lg-6">
      <div class="card p-4 shadow-sm">
        <div class="d-flex justify-content-between align-items-start mb-2">
          <div>
            <h4 class="mb-1">Créer une personne (cooptation)</h4>
            <div class="text-muted small">
              Tu es connecté : tu peux ajouter un nouveau profil CV.
            </div>
          </div>

          <RouterLink to="/cvs" class="btn btn-sm btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> Retour
          </RouterLink>
        </div>

        <!-- success -->
        <div
          v-if="ok"
          class="alert alert-success py-2 small d-flex align-items-center gap-2"
        >
          <i class="bi bi-check-circle"></i>
          <div class="flex-grow-1">Personne créée avec succès !</div>
        </div>

        <!-- error -->
        <div
          v-if="error"
          class="alert alert-danger py-2 small d-flex align-items-center gap-2"
        >
          <i class="bi bi-x-circle"></i>
          <div class="flex-grow-1">{{ error }}</div>
          <button type="button" class="btn btn-sm btn-outline-light" @click="error=null">
            Fermer
          </button>
        </div>

        <form @submit.prevent="create" class="vstack gap-2 mt-2">
          <div class="row g-2">
            <div class="col">
              <label class="form-label small text-muted">Prénom</label>
              <input v-model.trim="firstName" class="form-control" required autofocus>
            </div>
            <div class="col">
              <label class="form-label small text-muted">Nom</label>
              <input v-model.trim="lastName" class="form-control" required>
            </div>
          </div>

          <div>
            <label class="form-label small text-muted">Email</label>
            <input
              v-model.trim="email"
              type="email"
              class="form-control"
              required
              autocomplete="email"
            >
          </div>

          <div>
            <label class="form-label small text-muted">Site web (optionnel)</label>
            <input
              v-model.trim="website"
              class="form-control"
              placeholder="https://..."
            >
            <div
              v-if="website && !isValidUrl(website)"
              class="form-text text-danger small"
            >
              L’URL doit commencer par http:// ou https://
            </div>
          </div>

          <div>
            <label class="form-label small text-muted">Date de naissance (optionnel)</label>
            <input v-model="birthDate" type="date" class="form-control">
          </div>

          <div>
            <label class="form-label small text-muted">Mot de passe</label>
            <input
              v-model.trim="rawPassword"
              type="password"
              class="form-control"
              minlength="6"
              required
              autocomplete="new-password"
            >
          </div>

          <button
            class="btn btn-success mt-2"
            :disabled="loading || (website && !isValidUrl(website))"
          >
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            Créer la personne
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'

const router = useRouter()

const firstName = ref('')
const lastName = ref('')
const email = ref('')
const website = ref('')
const birthDate = ref('')
const rawPassword = ref('')

const ok = ref(false)
const error = ref(null)
const loading = ref(false)

function isValidUrl(v) {
  if (!v) return true
  return /^https?:\/\//i.test(v)
}

function normalizeCreateError(e) {
  const status = e?.response?.status
  const data = e?.response?.data

  if (status === 409) return "Cet email est déjà utilisé."
  if (status === 400) {
    const fieldError =
      data?.errors?.[0]?.defaultMessage ||
      data?.fieldErrors?.[0]?.defaultMessage
    if (fieldError) return fieldError
    if (data?.message) return data.message
    return "Données invalides. Vérifie les champs."
  }
  if (data?.message) return data.message
  return "Erreur serveur. Réessaie."
}

async function create() {
  ok.value = false
  error.value = null
  loading.value = true

  try {
    await api.post('/persons', {
      firstName: firstName.value.trim(),
      lastName: lastName.value.trim(),
      email: email.value.trim().toLowerCase(),
      website: website.value.trim() || null,
      birthDate: birthDate.value || null,
      rawPassword: rawPassword.value.trim(),
    })

    ok.value = true

    firstName.value = ''
    lastName.value = ''
    email.value = ''
    website.value = ''
    birthDate.value = ''
    rawPassword.value = ''

    setTimeout(() => router.push('/cvs'), 800)
  } catch (e) {
    error.value = normalizeCreateError(e)
  } finally {
    loading.value = false
  }
}
</script>
