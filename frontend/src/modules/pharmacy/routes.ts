import type { RouteRecordRaw } from 'vue-router'

export const pharmacyRoutes: RouteRecordRaw[] = [
  {
    path: 'pharmacy',
    name: 'pharmacy',
    component: () => import('@/components/ModuleWorkspace.vue'),
    props: { moduleCode: 'pharmacy', title: '药房管理', accent: '#7c3aed' },
    meta: { title: '药房管理' },
  },
]

