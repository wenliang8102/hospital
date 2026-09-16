import type { RouteRecordRaw } from 'vue-router'

export const registrationRoutes: RouteRecordRaw[] = [
  {
    path: 'registration',
    name: 'registration',
    component: () => import('./views/RegistrationView.vue'),
    meta: { title: '挂号收费' },
  },
]
