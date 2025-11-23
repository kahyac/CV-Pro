import { createRouter, createWebHistory } from 'vue-router'
import CvBrowseView from '../views/CvBrowseView.vue'
import LoginView from '../views/LoginView.vue'
import CvEditView from '../views/CvEditView.vue'
import PersonCreateView from '../views/PersonCreateView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/cvs' },

    { path: '/cvs', component: CvBrowseView },

    { path: '/login', component: LoginView },

    {
      path: '/cv/edit',
      component: CvEditView,
      meta: { requiresAuth: true }
    },

    {
      path: '/register',
      component: PersonCreateView,
      meta: { requiresAuth: true }
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
