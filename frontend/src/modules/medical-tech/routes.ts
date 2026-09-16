import type { RouteRecordRaw } from 'vue-router'

export const medicalTechRoutes: RouteRecordRaw[] = [
  {
    path: 'medical-tech',
    name: 'medical-tech',
    component: () => import('./views/MedicalTechExecutionView.vue'),
    meta: { title: '医技执行' },
  },
]
