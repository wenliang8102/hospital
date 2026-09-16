import type { RouteRecordRaw } from 'vue-router'

export const masterDataRoutes: RouteRecordRaw[] = [
  {
    path: 'master-data',
    name: 'master-data',
    component: () => import('./views/MasterDataView.vue'),
    meta: { title: '基础数据', permission: 'master-data:read' },
  },
]
