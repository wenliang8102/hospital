// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AppLayout from './AppLayout.vue'
import { useAuthStore } from '@/stores/auth'

const push = vi.fn()
const replace = vi.fn()

vi.mock('vue-router', () => ({
  RouterView: { template: '<main />' },
  useRoute: () => ({ path: '/', meta: { title: '工作台' } }),
  useRouter: () => ({ push, replace }),
}))

vi.mock('@/core/http', () => ({
  http: {
    post: vi.fn(),
  },
}))

function mountLayout(permissions: string[]) {
  const pinia = createPinia()
  setActivePinia(pinia)
  const auth = useAuthStore()
  auth.token = 'test-token'
  auth.user = {
    id: 1,
    username: 'tester',
    displayName: '测试员',
    employeeId: null,
    roles: ['TESTER'],
    permissions,
  }

  return mount(AppLayout, {
    global: {
      plugins: [pinia],
      stubs: {
        ElDrawer: { template: '<aside><slot /></aside>' },
        ElIcon: { template: '<i><slot /></i>' },
        ElMenu: { template: '<nav><slot /></nav>' },
        ElMenuItem: { template: '<a><slot /></a>' },
        ElTooltip: { template: '<span><slot /></span>' },
      },
    },
  })
}

describe('AppLayout', () => {
  beforeEach(() => {
    push.mockReset()
    replace.mockReset()
  })

  it('hides platform navigation when platform manage permission is missing', () => {
    const wrapper = mountLayout(['master-data:read'])

    expect(wrapper.text()).toContain('基础数据')
    expect(wrapper.text()).not.toContain('平台账号')
    expect(wrapper.text()).not.toContain('操作日志')
  })
})
