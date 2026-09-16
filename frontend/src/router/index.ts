import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import { moduleRoutes } from '@/modules/routes'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: moduleRoutes,
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && auth.isAuthenticated) {
    return { path: '/' }
  }
  const permissions = to.matched.flatMap((record) => {
    const metaPermissions = record.meta.permissions
    const metaPermission = record.meta.permission
    return [
      ...(Array.isArray(metaPermissions) ? metaPermissions : []),
      ...(typeof metaPermission === 'string' ? [metaPermission] : []),
    ]
  })
  if (permissions.length > 0 && !auth.hasAnyPermission(permissions)) {
    return { path: '/' }
  }
  return true
})

export default router
