<template>
  <div>
    <h3 class="mb-2">Mon profil</h3>
    <div class="text-muted small mb-3">Modifie tes coordonnées personnelles</div>

    <div v-if="error" class="alert alert-danger small">{{ error }}</div>
    <div v-if="success" class="alert alert-success small">{{ success }}</div>

    <div v-if="!auth.me" class="text-muted">Chargement...</div>

    <form v-else @submit.prevent="save" class="card p-3 vstack gap-2">
      <div class="row g-2">
        <div class="col">
          <label class="form-label small text-muted">Prénom</label>
          <input v-model.trim="form.firstName" class="form-control" required maxlength="100" />
        </div>
        <div class="col">
          <label class="form-label small text-muted">Nom</label>
          <input v-model.trim="form.lastName" class="form-control" required maxlength="100" />
        </div>
      </div>

      <div>
        <label class="form-label small text-muted">Website</label>
        <input v-model.trim="form.website" class="form-control" placeholder="https://..." />
      </div>

      <div>
        <label class="form-label small text-muted">Date de naissance</label>
        <input v-model="form.birthDate" type="date" class="form-control" />
      </div>

      <button class="btn btn-dark mt-2" :disabled="saving">
        <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
        Enregistrer
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const form = ref({
  firstName: '',
  lastName: '',
  website: '',
  birthDate: ''
})

const saving = ref(false)
const error = ref(null)
const success = ref(null)

onMounted(async () => {
  if (!auth.me) await auth.fetchMe()
  if (auth.me) {
    form.value.firstName = auth.me.firstName
    form.value.lastName = auth.me.lastName
    form.value.website = auth.me.website || ''
    form.value.birthDate = auth.me.birthDate || ''
  }
})

async function save() {
  error.value = null
  success.value = null
  saving.value = true
  try {
    await api.put(`/persons/${auth.me.id}`, {
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      website: form.value.website || null,
      birthDate: form.value.birthDate || null
    })

    success.value = "Profil mis à jour ✅"
    await auth.fetchMe()
  } catch (e) {
    error.value = e?.response?.data?.message || "Erreur mise à jour."
  } finally {
    saving.value = false
  }
}
</script>
