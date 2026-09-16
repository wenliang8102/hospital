import type { RouteRecordRaw } from 'vue-router'

export const masterDataRoutes: RouteRecordRaw[] = [
  {
    path: 'master-data',
    name: 'master-data',
    component: () => import('@/components/ModuleWorkspace.vue'),
    props: { moduleCode: 'master-data', title: '基础数据', accent: '#475569' },
    meta: { title: '基础数据' },
  },
]

