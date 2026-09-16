import type { RouteRecordRaw } from 'vue-router'

export const outpatientRoutes: RouteRecordRaw[] = [
  {
    path: 'outpatient',
    name: 'outpatient',
    component: () => import('@/components/ModuleWorkspace.vue'),
    props: { moduleCode: 'outpatient', title: '门诊诊疗', accent: '#2563eb' },
    meta: { title: '门诊诊疗' },
  },
]

