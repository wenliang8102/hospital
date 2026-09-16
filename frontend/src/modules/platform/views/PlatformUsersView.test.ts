// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import PlatformUsersView from './PlatformUsersView.vue'
import { http } from '@/core/http'

vi.mock('@/core/http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
  },
}))

const rolesResponse = {
  data: {
    code: 'OK',
    message: 'success',
    timestamp: '2026-09-16T12:00:00Z',
    data: [{ code: 'OUTPATIENT_DOCTOR', name: '门诊医生' }],
  },
}

const employeesResponse = {
  data: {
    code: 'OK',
    message: 'success',
    timestamp: '2026-09-16T12:00:00Z',
    data: {
      items: [{ id: 1, realName: '李医生', departmentName: '心内科', active: true }],
      page: 1,
      size: 100,
      total: 1,
    },
  },
}

const usersResponse = {
  data: {
    code: 'OK',
    message: 'success',
    timestamp: '2026-09-16T12:00:00Z',
    data: {
      items: [
        {
          id: 1,
          username: 'doctor-li',
          displayName: '李医生账号',
          employeeId: 1,
          employeeName: '李医生',
          enabled: true,
          roles: [{ code: 'OUTPATIENT_DOCTOR', name: '门诊医生' }],
        },
      ],
      page: 1,
      size: 20,
      total: 1,
    },
  },
}

function mountView() {
  return mount(PlatformUsersView, {
    global: {
      stubs: {
        ElButton: { template: '<button type="button" @click="$emit(\'click\')"><slot /></button>' },
        ElCheckbox: { template: '<label><slot /></label>' },
        ElCheckboxGroup: { template: '<div><slot /></div>' },
        ElDialog: { template: '<div><slot /><slot name="footer" /></div>' },
        ElForm: { template: '<form><slot /></form>' },
        ElFormItem: { template: '<label><slot /></label>' },
        ElIcon: { template: '<i><slot /></i>' },
        ElInput: { template: '<input />' },
        ElOption: { template: '<option><slot /></option>' },
        ElPagination: { template: '<nav />' },
        ElSelect: { template: '<select><slot /></select>' },
        ElSwitch: { template: '<input type="checkbox" />' },
        ElTable: {
          props: ['data'],
          template: '<div><div v-for="row in data" :key="row.id">{{ Object.values(row).join(" ") }}</div><slot /></div>',
        },
        ElTableColumn: { template: '<div />' },
        ElTag: { template: '<span><slot /></span>' },
        ElTooltip: { template: '<span><slot /></span>' },
      },
      directives: {
        loading: {},
      },
    },
  })
}

describe('PlatformUsersView', () => {
  beforeEach(() => {
    vi.mocked(http.get).mockImplementation((url) => {
      if (url === '/platform/roles') return Promise.resolve(rolesResponse)
      if (url === '/master-data/employees/manage') return Promise.resolve(employeesResponse)
      return Promise.resolve(usersResponse)
    })
  })

  it('loads roles, employee options, and user account rows', async () => {
    const wrapper = mountView()

    await flushPromises()

    expect(http.get).toHaveBeenCalledWith('/platform/roles')
    expect(http.get).toHaveBeenCalledWith('/master-data/employees/manage', {
      params: { active: true, page: 1, size: 100 },
    })
    expect(http.get).toHaveBeenCalledWith('/platform/users', {
      params: {
        keyword: undefined,
        enabled: undefined,
        page: 1,
        size: 20,
      },
    })
    expect(wrapper.text()).toContain('doctor-li')
    expect(wrapper.text()).toContain('李医生账号')
    expect(wrapper.text()).toContain('门诊医生')
  })
})
