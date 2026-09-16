import type { RouteRecordRaw } from 'vue-router'

export const medicalTechRoutes: RouteRecordRaw[] = [
  {
    path: 'medical-tech',
    name: 'medical-tech',
    component: () => import('@/components/ModuleWorkspace.vue'),
    props: { moduleCode: 'medical-tech', title: '医技执行', accent: '#b45309' },
    meta: { title: '医技执行' },
  },
]

