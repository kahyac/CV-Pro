<template>
  <div class="row g-4">
    <!-- Colonne gauche : liste -->
    <div class="col-lg-5">
      <div class="d-flex align-items-center justify-content-between mb-2">
        <div>
          <h3 class="mb-0">Parcourir les CV</h3>
          <div class="text-muted small">Recherche par nom ou prénom</div>
        </div>
        <!-- total côté backend -->
        <span class="badge text-bg-secondary">
          {{ totalElements }} CV
        </span>
      </div>

      <!-- Alert erreur -->
      <div v-if="error" class="alert alert-danger py-2 small d-flex align-items-center gap-2">
        <i class="bi bi-x-circle"></i>
        <div class="flex-grow-1">{{ error }}</div>
        <button class="btn btn-sm btn-outline-light" @click="error = null">
          Fermer
        </button>
      </div>

      <!-- Recherche -->
      <div class="input-group mb-3">
        <span class="input-group-text bg-white">
          <i class="bi bi-search"></i>
        </span>
        <input
          v-model="q"
          class="form-control"
          placeholder="Ex: Kartout, Yacine..."
          @keyup.enter="doSearch"
        />
        <button class="btn btn-dark" @click="doSearch" :disabled="loading">
          Rechercher
        </button>
        <button
          v-if="q.trim()"
          class="btn btn-outline-secondary"
          @click="resetSearch"
          :disabled="loading"
          title="Réinitialiser"
        >
          <i class="bi bi-arrow-counterclockwise"></i>
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="card p-4 text-center">
        <div class="spinner-border" role="status"></div>
        <div class="text-muted mt-2">Chargement des CV...</div>
      </div>

      <!-- Empty -->
      <div v-else-if="!persons.length" class="card p-4 text-center">
        <i class="bi bi-inbox fs-2 text-muted"></i>
        <div class="mt-2">Aucun CV trouvé.</div>
      </div>

      <!-- Liste -->
      <div v-else class="list-group">
        <button
          v-for="p in persons"
          :key="p.id"
          type="button"
          class="list-group-item list-group-item-action d-flex align-items-center gap-2"
          :class="{ active: selected?.id === p.id }"
          @click="selectPerson(p.id)"
        >
          <div
            class="rounded-circle bg-light d-flex justify-content-center align-items-center flex-shrink-0"
            style="width:36px;height:36px;"
          >
            <i class="bi bi-person text-dark"></i>
          </div>

          <div class="flex-grow-1 text-start">
            <div class="fw-semibold">{{ p.firstName }} {{ p.lastName }}</div>
            <div class="small opacity-75">{{ p.email }}</div>
          </div>

          <i class="bi bi-chevron-right opacity-75"></i>
        </button>
      </div>

      <!-- Pagination -->
      <nav v-if="totalPages > 1 && !loading" class="mt-3">
        <ul class="pagination pagination-sm justify-content-center mb-0">
          <li class="page-item" :class="{ disabled: page === 0 }">
            <button class="page-link" @click="goToPage(page - 1)" :disabled="page === 0">
              ‹
            </button>
          </li>

          <li
            v-for="p in pageNumbers"
            :key="p"
            class="page-item"
            :class="{ active: p === page }"
          >
            <button class="page-link" @click="goToPage(p)">
              {{ p + 1 }}
            </button>
          </li>

          <li class="page-item" :class="{ disabled: page >= totalPages - 1 }">
            <button
              class="page-link"
              @click="goToPage(page + 1)"
              :disabled="page >= totalPages - 1"
            >
              ›
            </button>
          </li>
        </ul>

        <div class="text-center small text-muted mt-1">
          Page {{ page + 1 }} / {{ totalPages }}
        </div>
      </nav>
    </div>

    <!-- Colonne droite : détails -->
    <div class="col-lg-7">
      <div v-if="!selected" class="card p-5 text-center h-100">
        <i class="bi bi-file-earmark-person fs-1 text-muted"></i>
        <p class="mt-3 mb-0 text-muted">
          Sélectionne une personne pour afficher son CV.
        </p>
      </div>

      <div v-else class="card p-4">
        <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
          <div>
            <h4 class="mb-0">{{ selected.firstName }} {{ selected.lastName }}</h4>
            <div class="text-muted small">{{ selected.email }}</div>
            <a
              v-if="selected.website"
              class="small text-decoration-none"
              :href="selected.website"
              target="_blank"
              rel="noopener"
            >
              <i class="bi bi-globe me-1"></i>{{ selected.website }}
            </a>
          </div>

          <div class="text-muted small">
            {{ selected.activities?.length || 0 }} activités
          </div>
        </div>

        <hr>

        <ul v-if="selected.activities?.length" class="list-group list-group-flush">
          <li
            v-for="a in selected.activities"
            :key="a.id"
            class="list-group-item"
          >
            <div class="d-flex justify-content-between align-items-center mb-1">
              <div class="fw-semibold">{{ a.title }}</div>
              <span class="badge text-bg-light border">{{ a.year }}</span>
            </div>

            <div class="small text-muted mb-1">{{ a.type }}</div>

            <div class="small">{{ a.description }}</div>

            <a
              v-if="a.url"
              class="small d-inline-block mt-2 text-decoration-none"
              :href="a.url"
              target="_blank"
              rel="noopener"
            >
              <i class="bi bi-link-45deg"></i> {{ a.url }}
            </a>
          </li>
        </ul>

        <div v-else class="text-muted small">
          Aucun détail d’activité pour ce CV.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, computed, watch} from 'vue'
import api from '@/services/api'

const persons = ref([])
const selected = ref(null)
const q = ref('')
const loading = ref(false)
const error = ref(null)

const page = ref(0)
const size = ref(50)
const totalPages = ref(0)
const totalElements = ref(0)

let searchTimer = null

const isSearching = computed(() => !!q.value.trim())

const pageNumbers = computed(() => {
  // afficher max 5 pages autour
  const t = totalPages.value
  const cur = page.value
  if (t <= 5) return Array.from({length: t}, (_, i) => i)

  const start = Math.max(0, cur - 2)
  const end = Math.min(t - 1, cur + 2)
  const arr = []
  for (let i = start; i <= end; i++) arr.push(i)
  return arr
})

async function fetchPersons() {
  loading.value = true
  error.value = null
  try {
    const url = isSearching.value
      ? `/persons/search?q=${encodeURIComponent(q.value.trim())}&page=${page.value}&size=${size.value}`
      : `/persons?page=${page.value}&size=${size.value}`

    const res = await api.get(url)

    persons.value = res.data.content
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements
  } catch (e) {
    error.value = "Impossible de charger les CV. Vérifie ton backend."
  } finally {
    loading.value = false
  }
}

async function selectPerson(id) {
  loading.value = true
  error.value = null
  try {
    const res = await api.get(`/persons/${id}`)
    selected.value = res.data
  } catch (e) {
    error.value = "Impossible de charger ce CV."
  } finally {
    loading.value = false
  }
}

function doSearch() {
  page.value = 0
  selected.value = null
  fetchPersons()
}

function resetSearch() {
  q.value = ''
  page.value = 0
  selected.value = null
  fetchPersons()
}

function goToPage(p) {
  if (p < 0 || p >= totalPages.value) return
  page.value = p
  selected.value = null
  fetchPersons()
}

watch(q, (val) => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 0
    selected.value = null
    fetchPersons()
  }, 400)
})

onMounted(fetchPersons)
</script>
