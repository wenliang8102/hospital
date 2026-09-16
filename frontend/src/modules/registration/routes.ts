import type { RouteRecordRaw } from 'vue-router'

export const registrationRoutes: RouteRecordRaw[] = [
  {
    path: 'registration',
    name: 'registration',
    component: () => import('@/components/ModuleWorkspace.vue'),
    props: { moduleCode: 'registration', title: '挂号收费', accent: '#0f766e' },
    meta: { title: '挂号收费' },
  },
]

