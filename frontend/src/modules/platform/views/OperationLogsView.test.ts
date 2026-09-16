// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import OperationLogsView from './OperationLogsView.vue'
import { http } from '@/core/http'

vi.mock('@/core/http', () => ({
  http: {
    get: vi.fn(),
  },
}))

const logsResponse = {
  data: {
    code: 'OK',
    message: 'success',
    timestamp: '2026-09-16T12:00:00Z',
    data: {
      items: [
        {
          id: 1,
          operatorId: 1,
          moduleCode: 'platform',
          action: 'USER_CREATE',
          targetType: 'USER',
          targetId: '3',
          detail: '创建账号 doctor-li',
          createdAt: '2026-09-16T13:58:14',
        },
      ],
      page: 1,
      size: 20,
      total: 1,
    },
  },
}

function mountView() {
  return mount(OperationLogsView, {
    global: {
      stubs: {
        ElButton: { template: '<button type="button" @click="$emit(\'click\')"><slot /></button>' },
        ElIcon: { template: '<i><slot /></i>' },
        ElInput: { template: '<input />' },
        ElOption: { template: '<option><slot /></option>' },
        ElPagination: { template: '<nav />' },
        ElSelect: { template: '<select><slot /></select>' },
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

describe('OperationLogsView', () => {
  beforeEach(() => {
    vi.mocked(http.get).mockResolvedValue(logsResponse)
  })

  it('loads and renders operation logs', async () => {
    const wrapper = mountView()

    await flushPromises()

    expect(http.get).toHaveBeenCalledWith('/platform/operation-logs', {
      params: {
        moduleCode: undefined,
        action: undefined,
        keyword: undefined,
        page: 1,
        size: 20,
      },
    })
    expect(wrapper.text()).toContain('USER_CREATE')
    expect(wrapper.text()).toContain('创建账号 doctor-li')
    expect(wrapper.text()).toContain('platform')
  })
})
