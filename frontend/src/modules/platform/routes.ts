import type { RouteRecordRaw } from 'vue-router'

export const platformRoutes: RouteRecordRaw[] = [
  {
    path: 'platform/users',
    name: 'platform-users',
    component: () => import('./views/PlatformUsersView.vue'),
    meta: { title: '平台账号', permission: 'platform:manage' },
  },
  {
    path: 'platform/logs',
    name: 'platform-operation-logs',
    component: () => import('./views/OperationLogsView.vue'),
    meta: { title: '操作日志', permission: 'platform:manage' },
  },
]
