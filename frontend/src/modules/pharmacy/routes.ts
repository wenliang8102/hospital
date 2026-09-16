import type { RouteRecordRaw } from 'vue-router'

export const pharmacyRoutes: RouteRecordRaw[] = [
  {
    path: 'pharmacy',
    name: 'pharmacy',
    component: () => import('./views/PharmacyDispenseView.vue'),
    meta: { title: '药房管理' },
  },
]
