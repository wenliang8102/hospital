import type { RouteRecordRaw } from 'vue-router'

export const outpatientRoutes: RouteRecordRaw[] = [
  {
    path: 'outpatient',
    name: 'outpatient',
    component: () => import('./views/OutpatientView.vue'),
    meta: { title: '门诊诊疗' },
  },
]
