<template>
  <header class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top shadow-sm">
    <div class="container">
      <RouterLink class="navbar-brand fw-semibold d-flex align-items-center gap-2" to="/cvs">
        <i class="bi bi-file-earmark-person"></i>
        CurriculumVitaePro
      </RouterLink>

      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#mainNav"
        aria-controls="mainNav"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div id="mainNav" class="collapse navbar-collapse">
        <ul class="navbar-nav me-auto gap-lg-1">

          <!-- MON PROFIL -->
          <li class="nav-item" v-if="auth.isAuthenticated">
            <RouterLink class="nav-link" active-class="active fw-semibold" to="/profile">
              Mon profil
            </RouterLink>
          </li>

          <li class="nav-item">
            <RouterLink class="nav-link" active-class="active fw-semibold" to="/cvs">
              Parcourir CV
            </RouterLink>
          </li>

          <li class="nav-item" v-if="auth.isAuthenticated">
            <RouterLink class="nav-link" active-class="active fw-semibold" to="/cv/edit">
              Modifier mon CV
            </RouterLink>
          </li>

          <li class="nav-item" v-if="auth.isAuthenticated">
            <RouterLink class="nav-link" active-class="active fw-semibold" to="/register">
              Coopter une personne
            </RouterLink>
          </li>

          <li class="nav-item" v-else>
            <span class="nav-link disabled opacity-50">
              Cooptation (login requis)
            </span>
          </li>
        </ul>

        <div class="d-flex align-items-center gap-3">

          <RouterLink
            v-if="!auth.isAuthenticated"
            class="btn btn-outline-light btn-sm px-3"
            to="/login"
          >
            <i class="bi bi-box-arrow-in-right me-1"></i>
            Login
          </RouterLink>

          <div v-if="auth.isAuthenticated && auth.me" class="text-white small">
            Connecté en tant que
            <strong>{{ auth.me.firstName }} {{ auth.me.lastName }}</strong>
          </div>

          <button
            v-if="auth.isAuthenticated"
            class="btn btn-warning btn-sm px-3"
            @click="logout"
          >
            <i class="bi bi-box-arrow-right me-1"></i>
            Logout
          </button>

        </div>
      </div>
    </div>
  </header>

  <main class="container py-4">
    <RouterView />
  </main>

  <footer class="py-3 mt-auto">
    <div class="container text-center text-muted small">
      CurriculumVitaePro — Demo Vue + Spring Boot
    </div>
  </footer>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
const auth = useAuthStore()

onMounted(() => {
  if (auth.token) auth.fetchMe()
})

function logout() {
  auth.logout()
}
</script>
