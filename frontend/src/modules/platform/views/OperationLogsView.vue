<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { http, type ApiResponse } from '@/core/http'

interface OperationLog {
  id: number
  operatorId: number | null
  moduleCode: string
  action: string
  targetType?: string
  targetId?: string
  detail?: string
  createdAt: string
}

interface PageData<T> {
  items: T[]
  page: number
  size: number
  total: number
}

const actionOptions = [
  { value: 'USER_CREATE', label: '创建账号' },
  { value: 'USER_UPDATE', label: '更新账号' },
  { value: 'USER_ENABLE', label: '启用账号' },
  { value: 'USER_DISABLE', label: '停用账号' },
]

const logs = ref<OperationLog[]>([])
const loading = ref(false)
const total = ref(0)

const filters = reactive({
  moduleCode: '',
  action: '',
  keyword: '',
})

const pagination = reactive({
  page: 1,
  size: 20,
})

async function loadLogs() {
  loading.value = true
  try {
    const response = await http.get<ApiResponse<PageData<OperationLog>>>('/platform/operation-logs', {
      params: {
        moduleCode: filters.moduleCode.trim() || undefined,
        action: filters.action || undefined,
        keyword: filters.keyword.trim() || undefined,
        page: pagination.page,
        size: pagination.size,
      },
    })
    logs.value = response.data.data.items
    total.value = response.data.data.total
  } finally {
    loading.value = false
  }
}

function search() {
  pagination.page = 1
  void loadLogs()
}

function resetFilters() {
  filters.moduleCode = ''
  filters.action = ''
  filters.keyword = ''
  pagination.page = 1
  void loadLogs()
}

function handlePageChange(page: number) {
  pagination.page = page
  void loadLogs()
}

function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  void loadLogs()
}

function actionLabel(action: string) {
  return actionOptions.find((item) => item.value === action)?.label ?? action
}

onMounted(() => {
  void loadLogs()
})
</script>

<template>
  <section class="operation-logs-page">
    <div class="section-heading">
      <div>
        <p class="section-kicker">平台管理</p>
        <h2>操作日志</h2>
      </div>
    </div>

    <div class="logs-toolbar">
      <el-input v-model="filters.keyword" class="keyword-input" placeholder="动作、对象或详情" clearable @keyup.enter="search">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filters.moduleCode" class="filter-select" placeholder="模块" clearable>
        <el-option label="平台管理" value="platform" />
        <el-option label="基础数据" value="master-data" />
      </el-select>
      <el-select v-model="filters.action" class="filter-select" placeholder="动作" clearable>
        <el-option v-for="item in actionOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <div class="toolbar-actions">
        <el-tooltip content="查询" placement="top">
          <el-button type="primary" :icon="Search" circle @click="search" />
        </el-tooltip>
        <el-tooltip content="重置" placement="top">
          <el-button :icon="Refresh" circle @click="resetFilters" />
        </el-tooltip>
      </div>
    </div>

    <div class="logs-table">
      <el-table v-loading="loading" :data="logs" row-key="id">
        <el-table-column prop="createdAt" label="时间" min-width="170" />
        <el-table-column prop="moduleCode" label="模块" min-width="120" />
        <el-table-column label="动作" min-width="130">
          <template #default="{ row }">
            <el-tag effect="plain">{{ actionLabel(row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorId" label="操作人" min-width="110" />
        <el-table-column prop="targetType" label="对象类型" min-width="110" />
        <el-table-column prop="targetId" label="对象 ID" min-width="110" />
        <el-table-column prop="detail" label="详情" min-width="220" />
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pagination.page"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
.operation-logs-page {
  min-width: 0;
}

.logs-toolbar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(130px, 160px) minmax(130px, 170px) auto;
  gap: 12px;
  margin-bottom: 14px;
}

.keyword-input,
.filter-select {
  width: 100%;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logs-table {
  overflow: hidden;
  border: 1px solid var(--border);
  border-top: 3px solid var(--primary);
  border-radius: 7px;
  background: var(--surface);
}

.logs-table :deep(.el-table) {
  --el-table-header-bg-color: #f8fafb;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid var(--border);
}

@media (max-width: 780px) {
  .logs-toolbar {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    justify-content: flex-end;
  }

  .pagination-bar {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
