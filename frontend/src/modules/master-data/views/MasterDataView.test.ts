// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import MasterDataView from './MasterDataView.vue'
import { http } from '@/core/http'
import { useAuthStore } from '@/stores/auth'

vi.mock('@/core/http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
  },
}))

const departmentsResponse = {
  data: {
    code: 'OK',
    message: 'success',
    timestamp: '2026-09-16T12:00:00Z',
    data: {
      items: [
        { id: 1, code: 'CARD', name: '心内科', type: 'OUTPATIENT', active: true },
        { id: 2, code: 'LAB', name: '检验科', type: 'INSPECTION', active: false },
      ],
      page: 1,
      size: 20,
      total: 2,
    },
  },
}

const drugsResponse = {
  data: {
    code: 'OK',
    message: 'success',
    timestamp: '2026-09-16T12:00:00Z',
    data: {
      items: [
        {
          id: 1,
          code: 'DRUG001',
          name: '阿莫西林胶囊',
          format: '0.25g*24粒',
          unit: '盒',
          manufacturer: '示例药厂',
          dosage: '口服',
          type: '抗生素',
          price: 12.5,
          mnemonicCode: 'AMXL',
          active: true,
        },
      ],
      page: 1,
      size: 20,
      total: 1,
    },
  },
}

const employeesResponse = {
  data: {
    code: 'OK',
    message: 'success',
    timestamp: '2026-09-16T12:00:00Z',
    data: {
      items: [
        {
          id: 1,
          realName: '王医生',
          departmentId: 1,
          departmentName: '心内科',
          registLevelId: 1,
          registLevelName: '普通号',
          schedulingId: 1,
          schedulingName: '工作日排班',
          active: true,
        },
      ],
      page: 1,
      size: 20,
      total: 1,
    },
  },
}

const registLevelsResponse = {
  data: {
    code: 'OK',
    message: 'success',
    timestamp: '2026-09-16T12:00:00Z',
    data: [{ id: 1, code: 'GENERAL', name: '普通号', fee: 8, quota: 100 }],
  },
}

const schedulingResponse = {
  data: {
    code: 'OK',
    message: 'success',
    timestamp: '2026-09-16T12:00:00Z',
    data: {
      items: [
        {
          id: 1,
          name: '工作日排班',
          weekRule: '00111111111000',
          active: true,
        },
      ],
      page: 1,
      size: 20,
      total: 1,
    },
  },
}

function mountView(permissions = ['master-data:read', 'master-data:write']) {
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

  return mount(MasterDataView, {
    global: {
      plugins: [pinia],
      stubs: {
        ElButton: { template: '<button type="button" @click="$emit(\'click\')"><slot /></button>' },
        ElDialog: { template: '<div><slot /><slot name="footer" /></div>' },
        ElForm: {
          template: '<form><slot /></form>',
          methods: { validate: () => Promise.resolve(true) },
        },
        ElFormItem: { template: '<label><slot /></label>' },
        ElIcon: { template: '<i><slot /></i>' },
        ElInput: { template: '<input />' },
        ElOption: { template: '<option><slot /></option>' },
        ElPagination: { template: '<nav />' },
        ElSelect: { template: '<select><slot /></select>' },
        ElSwitch: { template: '<input type="checkbox" />' },
        ElTabs: {
          props: ['modelValue'],
          emits: ['update:modelValue', 'tabChange'],
          template: `
            <div>
              <button data-test="tab-departments" @click="$emit('update:modelValue', 'departments'); $emit('tabChange', 'departments')">科室管理</button>
              <button data-test="tab-employees" @click="$emit('update:modelValue', 'employees'); $emit('tabChange', 'employees')">员工</button>
              <button data-test="tab-drugs" @click="$emit('update:modelValue', 'drugs'); $emit('tabChange', 'drugs')">药品</button>
              <button data-test="tab-scheduling" @click="$emit('update:modelValue', 'scheduling'); $emit('tabChange', 'scheduling')">排班</button>
              <slot />
            </div>
          `,
        },
        ElTabPane: { template: '<section><slot /></section>' },
        ElTable: {
          props: ['data'],
          template:
            '<div><div v-for="row in data" :key="row.id">{{ Object.values(row).join(" ") }} {{ row.active === false ? "已停用" : "" }}</div><slot /></div>',
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

describe('MasterDataView', () => {
  beforeEach(() => {
    vi.mocked(http.get).mockImplementation((url) => {
      if (url === '/master-data/drugs/manage') return Promise.resolve(drugsResponse)
      if (url === '/master-data/scheduling/manage') return Promise.resolve(schedulingResponse)
      if (url === '/master-data/employees/manage') return Promise.resolve(employeesResponse)
      if (url === '/master-data/regist-levels') return Promise.resolve(registLevelsResponse)
      return Promise.resolve(departmentsResponse)
    })
    vi.mocked(http.post).mockResolvedValue({ data: { ...departmentsResponse.data, data: departmentsResponse.data.data.items[0] } })
    vi.mocked(http.put).mockResolvedValue({ data: { ...departmentsResponse.data, data: departmentsResponse.data.data.items[0] } })
    vi.mocked(http.patch).mockResolvedValue({ data: { ...departmentsResponse.data, data: departmentsResponse.data.data.items[0] } })
  })

  it('loads and renders department rows', async () => {
    const wrapper = mountView()

    await flushPromises()

    expect(http.get).toHaveBeenCalledWith('/master-data/departments/manage', {
      params: {
        keyword: undefined,
        type: undefined,
        active: undefined,
        page: 1,
        size: 20,
      },
    })
    expect(wrapper.text()).toContain('CARD')
    expect(wrapper.text()).toContain('心内科')
    expect(wrapper.text()).toContain('LAB')
    expect(wrapper.text()).toContain('已停用')
  })

  it('loads and renders managed lookup rows after switching tabs', async () => {
    const wrapper = mountView()

    await flushPromises()
    await wrapper.get('[data-test="tab-drugs"]').trigger('click')
    await flushPromises()

    expect(http.get).toHaveBeenCalledWith('/master-data/drugs/manage', {
      params: {
        keyword: undefined,
        active: undefined,
        page: 1,
        size: 20,
      },
    })
    expect(wrapper.text()).toContain('新增药品')
    expect(wrapper.text()).toContain('DRUG001')
    expect(wrapper.text()).toContain('阿莫西林胶囊')
    expect(wrapper.text()).toContain('AMXL')
  })

  it('creates managed drug rows through the maintenance endpoint', async () => {
    const wrapper = mountView()

    await flushPromises()
    await wrapper.get('[data-test="tab-drugs"]').trigger('click')
    await flushPromises()

    ;(wrapper.vm as unknown as {
      openCreateMaintenanceDialog: () => void
      maintenanceForm: Record<string, unknown>
      saveMaintenance: () => Promise<void>
    }).openCreateMaintenanceDialog()
    Object.assign((wrapper.vm as unknown as { maintenanceForm: Record<string, unknown> }).maintenanceForm, {
      code: 'DRUG002',
      name: '布洛芬片',
      format: '0.2g*20片',
      unit: '盒',
      manufacturer: '示例药厂',
      dosage: '口服',
      type: '西药',
      price: 8.5,
      mnemonicCode: 'BLF',
      active: true,
    })

    await (wrapper.vm as unknown as { saveMaintenance: () => Promise<void> }).saveMaintenance()

    expect(http.post).toHaveBeenCalledWith('/master-data/drugs/manage', {
      code: 'DRUG002',
      name: '布洛芬片',
      format: '0.2g*20片',
      unit: '盒',
      manufacturer: '示例药厂',
      dosage: '口服',
      type: '西药',
      price: 8.5,
      mnemonicCode: 'BLF',
      active: true,
    })
  })

  it('loads and creates scheduling rules through the maintenance endpoint', async () => {
    const wrapper = mountView()

    await flushPromises()
    await wrapper.get('[data-test="tab-scheduling"]').trigger('click')
    await flushPromises()

    expect(http.get).toHaveBeenCalledWith('/master-data/scheduling/manage', {
      params: {
        keyword: undefined,
        active: undefined,
        page: 1,
        size: 20,
      },
    })
    expect(wrapper.text()).toContain('新增排班')
    expect(wrapper.text()).toContain('工作日排班')
    expect(wrapper.text()).toContain('00111111111000')

    ;(wrapper.vm as unknown as {
      openCreateMaintenanceDialog: () => void
      maintenanceForm: Record<string, unknown>
      saveMaintenance: () => Promise<void>
    }).openCreateMaintenanceDialog()
    Object.assign((wrapper.vm as unknown as { maintenanceForm: Record<string, unknown> }).maintenanceForm, {
      name: '周末排班',
      weekRule: '11000000000011',
      active: true,
    })

    await (wrapper.vm as unknown as { saveMaintenance: () => Promise<void> }).saveMaintenance()

    expect(http.post).toHaveBeenCalledWith('/master-data/scheduling/manage', {
      name: '周末排班',
      weekRule: '11000000000011',
      active: true,
    })
  })

  it('loads managed employee rows after switching to employee tab', async () => {
    const wrapper = mountView()

    await flushPromises()
    await wrapper.get('[data-test="tab-employees"]').trigger('click')
    await flushPromises()

    expect(http.get).toHaveBeenCalledWith('/master-data/employees/manage', {
      params: {
        keyword: undefined,
        departmentId: undefined,
        active: undefined,
        page: 1,
        size: 20,
      },
    })
    expect(wrapper.text()).toContain('新增员工')
    expect(wrapper.text()).toContain('王医生')
    expect(wrapper.text()).toContain('心内科')
  })

  it('preserves registration level and scheduling when updating an employee', async () => {
    const wrapper = mountView()
    await flushPromises()

    const view = wrapper.vm as unknown as {
      employeeForm: Record<string, unknown>
      openEditEmployeeDialog: (row: Record<string, unknown>) => void
      saveEmployee: () => Promise<void>
    }
    view.openEditEmployeeDialog(employeesResponse.data.data.items[0])
    await flushPromises()
    await view.saveEmployee()

    expect(http.put).toHaveBeenCalledWith('/master-data/employees/manage/1', {
      realName: '王医生',
      departmentId: 1,
      registLevelId: 1,
      schedulingId: 1,
      active: true,
    })
  })

  it('hides write actions when user only has master data read permission', async () => {
    const wrapper = mountView(['master-data:read'])

    await flushPromises()

    expect(wrapper.text()).not.toContain('新增科室')

    await wrapper.get('[data-test="tab-drugs"]').trigger('click')
    await flushPromises()

    expect(http.get).toHaveBeenCalledWith('/master-data/drugs', {
      params: {
        keyword: undefined,
        page: 1,
        size: 20,
      },
    })
    expect(wrapper.text()).not.toContain('新增药品')
  })
})
