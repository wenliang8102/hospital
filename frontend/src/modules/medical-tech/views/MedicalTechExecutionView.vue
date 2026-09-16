<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Check, EditPen, Refresh, Search, VideoPlay, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { http, type ApiResponse } from '@/core/http'

type MedicalOrderType = 'CHECK' | 'INSPECTION' | 'DISPOSAL'
type MedicalOrderState =
  | 'CREATED'
  | 'PAID'
  | 'ACCEPTED'
  | 'EXECUTED'
  | 'RESULT_REPORTED'
  | 'REFUNDED'
  | 'CANCELLED'
type MedicalOrderStateFilter = MedicalOrderState | ''

interface PageResult<T> {
  items: T[]
  page: number
  size: number
  total: number
}

interface MedicalOrder {
  id: number
  type: MedicalOrderType
  registrationId: number
  medicalTechnologyId: number
  requestInfo?: string
  bodyPosition?: string
  executorEmployeeId?: number
  resultEmployeeId?: number
  executedAt?: string
  result?: string
  state: MedicalOrderState
  remark?: string
  createdAt: string
}

interface ApiErrorPayload {
  response?: {
    data?: {
      message?: string
    }
  }
}

const typeOptions: Array<{ label: string; value: MedicalOrderType }> = [
  { label: '检查', value: 'CHECK' },
  { label: '检验', value: 'INSPECTION' },
  { label: '处置', value: 'DISPOSAL' },
]

const stateOptions: Array<{ label: string; value: MedicalOrderStateFilter }> = [
  { label: '全部状态', value: '' },
  { label: '已缴费', value: 'PAID' },
  { label: '已接收', value: 'ACCEPTED' },
  { label: '已执行', value: 'EXECUTED' },
  { label: '已出结果', value: 'RESULT_REPORTED' },
  { label: '已退费', value: 'REFUNDED' },
  { label: '已取消', value: 'CANCELLED' },
]

const type = ref<MedicalOrderType>('CHECK')
const state = ref<MedicalOrderStateFilter>('PAID')
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const orders = ref<MedicalOrder[]>([])
const resultDialogVisible = ref(false)
const submittingResult = ref(false)
const activeOrder = ref<MedicalOrder | null>(null)
const resultDialogMode = ref<'edit' | 'view'>('edit')
const resultForm = reactive({
  result: '',
  remark: '',
})

const currentTypeLabel = computed(() => typeOptions.find((item) => item.value === type.value)?.label ?? '医技')
const currentStateLabel = computed(() => stateOptions.find((item) => item.value === state.value)?.label ?? '全部')
const resultDialogTitle = computed(() => (resultDialogMode.value === 'view' ? '查看执行结果' : '录入执行结果'))

function resetPageAndLoad() {
  page.value = 1
  void loadOrders()
}

async function loadOrders() {
  loading.value = true
  try {
    const response = await http.get<ApiResponse<PageResult<MedicalOrder>>>('/medical-orders', {
      params: {
        type: type.value,
        state: state.value || undefined,
        keyword: keyword.value.trim() || undefined,
        page: page.value,
        size: size.value,
      },
    })
    orders.value = response.data.data.items
    total.value = response.data.data.total
  } catch (error) {
    ElMessage.error(errorMessage(error, '医技队列加载失败'))
  } finally {
    loading.value = false
  }
}

async function acceptOrder(row: MedicalOrder) {
  try {
    await http.post(`/medical-orders/${row.type}/${row.id}/accept`)
    ElMessage.success('已接收申请')
    await loadOrders()
  } catch (error) {
    ElMessage.error(errorMessage(error, '接收申请失败'))
  }
}

async function executeOrder(row: MedicalOrder) {
  try {
    await http.post(`/medical-orders/${row.type}/${row.id}/execute`)
    ElMessage.success('已标记执行')
    await loadOrders()
  } catch (error) {
    ElMessage.error(errorMessage(error, '执行申请失败'))
  }
}

function openResultDialog(row: MedicalOrder, mode: 'edit' | 'view' = 'edit') {
  activeOrder.value = row
  resultDialogMode.value = mode
  resultForm.result = row.result ?? ''
  resultForm.remark = row.remark ?? ''
  resultDialogVisible.value = true
}

async function submitResult() {
  if (!activeOrder.value) return
  if (!resultForm.result.trim()) {
    ElMessage.warning('请录入执行结果')
    return
  }

  submittingResult.value = true
  try {
    await http.post(`/medical-orders/${activeOrder.value.type}/${activeOrder.value.id}/result`, {
      result: resultForm.result.trim(),
      remark: resultForm.remark.trim() || undefined,
    })
    ElMessage.success('结果已保存')
    resultDialogVisible.value = false
    await loadOrders()
  } catch (error) {
    ElMessage.error(errorMessage(error, '保存结果失败'))
  } finally {
    submittingResult.value = false
  }
}

function errorMessage(error: unknown, fallback: string) {
  return (error as ApiErrorPayload).response?.data?.message ?? fallback
}

function stateLabel(value: MedicalOrderState) {
  return stateOptions.find((item) => item.value === value)?.label ?? value
}

function stateTagType(value: MedicalOrderState) {
  if (value === 'PAID') return 'warning'
  if (value === 'ACCEPTED') return 'primary'
  if (value === 'EXECUTED') return 'info'
  if (value === 'RESULT_REPORTED') return 'success'
  if (value === 'REFUNDED' || value === 'CANCELLED') return 'danger'
  return 'info'
}

function formatDate(value?: string) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

onMounted(() => {
  void loadOrders()
})
</script>

<template>
  <section class="medical-tech-page">
    <div class="section-heading">
      <div>
        <p class="section-kicker">方向三 / 医技执行</p>
        <h2>医技执行台</h2>
      </div>
      <el-button :icon="Refresh" @click="loadOrders">刷新</el-button>
    </div>

    <div class="medical-tech-toolbar">
      <el-radio-group v-model="type" @change="resetPageAndLoad">
        <el-radio-button v-for="item in typeOptions" :key="item.value" :label="item.value">
          {{ item.label }}
        </el-radio-button>
      </el-radio-group>

      <div class="medical-tech-filters">
        <el-select v-model="state" class="state-filter" @change="resetPageAndLoad">
          <el-option v-for="item in stateOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input
          v-model="keyword"
          class="keyword-input"
          clearable
          placeholder="申请号 / 挂号 ID"
          @clear="resetPageAndLoad"
          @keyup.enter="resetPageAndLoad"
        />
        <el-button type="primary" :icon="Search" @click="resetPageAndLoad">查询</el-button>
      </div>
    </div>

    <section class="medical-tech-panel">
      <div class="panel-heading medical-tech-panel-heading">
        <div>
          <p class="section-kicker">{{ currentTypeLabel }}队列 / 历史</p>
          <h3>{{ currentStateLabel }}申请</h3>
        </div>
        <el-tag effect="plain">共 {{ total }} 项</el-tag>
      </div>

      <el-table v-loading="loading" :data="orders" table-layout="fixed" class="medical-tech-table">
        <el-table-column prop="id" label="申请号" width="100" />
        <el-table-column prop="registrationId" label="挂号 ID" width="110" />
        <el-table-column prop="medicalTechnologyId" label="项目 ID" width="110" />
        <el-table-column label="申请说明" min-width="220">
          <template #default="{ row }">
            <div class="order-main">
              <strong>{{ row.requestInfo || '未填写申请说明' }}</strong>
              <span>部位：{{ row.bodyPosition || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="stateTagType(row.state)" effect="plain">{{ stateLabel(row.state) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="150">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <div class="action-row">
              <el-button v-if="row.state === 'PAID'" type="primary" size="small" :icon="Check" @click="acceptOrder(row)">
                接收
              </el-button>
              <el-button v-if="row.state === 'ACCEPTED'" type="primary" size="small" :icon="VideoPlay" @click="executeOrder(row)">
                执行
              </el-button>
              <el-button v-if="row.state === 'EXECUTED'" type="primary" size="small" :icon="EditPen" @click="openResultDialog(row)">
                录结果
              </el-button>
              <el-button v-if="row.state === 'RESULT_REPORTED'" type="success" size="small" :icon="View" @click="openResultDialog(row, 'view')">
                看结果
              </el-button>
              <span v-if="!['PAID', 'ACCEPTED', 'EXECUTED', 'RESULT_REPORTED'].includes(row.state)" class="muted-action">无需操作</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="resetPageAndLoad"
          @current-change="loadOrders"
        />
      </div>
    </section>

    <el-dialog v-model="resultDialogVisible" :title="resultDialogTitle" width="560px">
      <div v-if="activeOrder" class="result-meta">
        <span>申请号：{{ activeOrder.id }}</span>
        <span>接收人 ID：{{ activeOrder.executorEmployeeId ?? '-' }}</span>
        <span>报告人 ID：{{ activeOrder.resultEmployeeId ?? '-' }}</span>
        <span>执行时间：{{ formatDate(activeOrder.executedAt) }}</span>
      </div>
      <el-form label-position="top">
        <el-form-item label="执行结果" required>
          <el-input
            v-model="resultForm.result"
            type="textarea"
            :rows="5"
            maxlength="2000"
            show-word-limit
            :disabled="resultDialogMode === 'view'"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="resultForm.remark"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            :disabled="resultDialogMode === 'view'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultDialogVisible = false">{{ resultDialogMode === 'view' ? '关闭' : '取消' }}</el-button>
        <el-button v-if="resultDialogMode === 'edit'" type="primary" :loading="submittingResult" @click="submitResult">保存结果</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.medical-tech-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.state-filter {
  width: 160px;
}

.medical-tech-filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

.keyword-input {
  width: 220px;
}

.medical-tech-panel {
  border: 1px solid var(--border);
  border-top: 3px solid #b45309;
  border-radius: 7px;
  background: var(--surface);
}

.medical-tech-panel-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.medical-tech-table {
  --el-table-header-bg-color: #f8fafb;
}

.order-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-main strong {
  font-size: 13px;
  font-weight: 600;
  overflow-wrap: anywhere;
}

.order-main span,
.muted-action {
  color: var(--muted);
  font-size: 12px;
}

.action-row {
  min-height: 32px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 6px;
  background: #f8fafb;
  color: var(--muted);
  font-size: 12px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px 16px;
  border-top: 1px solid #edf0f2;
}

@media (max-width: 720px) {
  .medical-tech-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .state-filter {
    width: 100%;
  }

  .medical-tech-filters {
    align-items: stretch;
    flex-direction: column;
  }

  .keyword-input {
    width: 100%;
  }

  .pagination-row {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .result-meta {
    grid-template-columns: 1fr;
  }
}
</style>
