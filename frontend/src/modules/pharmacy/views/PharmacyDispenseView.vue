<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Check, EditPen, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http, type ApiResponse } from '@/core/http'

type PrescriptionState = 'CREATED' | 'PAID' | 'DISPENSED' | 'RETURNED' | 'REFUNDED' | 'CANCELLED'
type StockOperationMode = 'inbound' | 'adjust'

interface PageResult<T> {
  items: T[]
  page: number
  size: number
  total: number
}

interface PharmacyPrescription {
  id: number
  registrationId: number
  drugId: number
  drugUsage: string
  drugNumber: number
  state: PrescriptionState
  createdAt: string
  dispensedAt?: string
  dispensedBy?: number
  stockQuantity?: number
}

interface StockTransaction {
  id: number
  drugId: number
  prescriptionId?: number
  transactionType: 'DISPENSE' | 'RETURN' | string
  quantity: number
  quantityBefore: number
  quantityAfter: number
  operatorUserId?: number
  createdAt: string
}

interface DrugStock {
  drugId: number
  quantity: number
  version: number
  updatedAt: string
}

interface ApiErrorPayload {
  response?: {
    data?: {
      message?: string
    }
  }
}

const stateOptions: Array<{ label: string; value: PrescriptionState }> = [
  { label: '已缴费', value: 'PAID' },
  { label: '已发药', value: 'DISPENSED' },
  { label: '已退药', value: 'RETURNED' },
  { label: '已退费', value: 'REFUNDED' },
  { label: '已取消', value: 'CANCELLED' },
]

const keyword = ref('')
const state = ref<PrescriptionState>('PAID')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const transactionDrugId = ref('')
const transactionPrescriptionId = ref('')
const transactionPage = ref(1)
const transactionSize = ref(10)
const transactionTotal = ref(0)
const transactionLoading = ref(false)
const stockKeyword = ref('')
const stockMaxQuantity = ref<number | undefined>(20)
const stockPage = ref(1)
const stockSize = ref(10)
const stockTotal = ref(0)
const stockLoading = ref(false)
const stockOperationDialogVisible = ref(false)
const stockOperationSubmitting = ref(false)
const stockOperationMode = ref<StockOperationMode>('inbound')
const dispensingId = ref<number | null>(null)
const returningId = ref<number | null>(null)
const prescriptions = ref<PharmacyPrescription[]>([])
const transactions = ref<StockTransaction[]>([])
const stocks = ref<DrugStock[]>([])
const stockOperationForm = reactive<{
  drugId?: number
  quantity?: number
  targetQuantity?: number
}>({})

const currentStateLabel = computed(() => stateOptions.find((item) => item.value === state.value)?.label ?? '处方')
const stockOperationTitle = computed(() => (stockOperationMode.value === 'inbound' ? '药品入库' : '库存调整'))

async function refreshAll() {
  await Promise.all([loadPrescriptions(), loadStocks(), loadTransactions()])
}

function resetPageAndLoad() {
  page.value = 1
  void loadPrescriptions()
}

async function loadPrescriptions() {
  loading.value = true
  try {
    const response = await http.get<ApiResponse<PageResult<PharmacyPrescription>>>('/pharmacy/prescriptions', {
      params: {
        keyword: keyword.value.trim() || undefined,
        state: state.value,
        page: page.value,
        size: size.value,
      },
    })
    prescriptions.value = response.data.data.items
    total.value = response.data.data.total
  } catch (error) {
    ElMessage.error(errorMessage(error, '处方队列加载失败'))
  } finally {
    loading.value = false
  }
}

function resetStockPageAndLoad() {
  stockPage.value = 1
  void loadStocks()
}

async function loadStocks() {
  stockLoading.value = true
  try {
    const response = await http.get<ApiResponse<PageResult<DrugStock>>>('/pharmacy/stocks', {
      params: {
        keyword: stockKeyword.value.trim() || undefined,
        maxQuantity: stockMaxQuantity.value ?? undefined,
        page: stockPage.value,
        size: stockSize.value,
      },
    })
    stocks.value = response.data.data.items
    stockTotal.value = response.data.data.total
  } catch (error) {
    ElMessage.error(errorMessage(error, '低库存数据加载失败'))
  } finally {
    stockLoading.value = false
  }
}

function openInboundDialog(row?: DrugStock) {
  stockOperationMode.value = 'inbound'
  stockOperationForm.drugId = row?.drugId ?? parseNumericKeyword(stockKeyword.value)
  stockOperationForm.quantity = undefined
  stockOperationForm.targetQuantity = undefined
  stockOperationDialogVisible.value = true
}

function openAdjustmentDialog(row: DrugStock) {
  stockOperationMode.value = 'adjust'
  stockOperationForm.drugId = row.drugId
  stockOperationForm.quantity = undefined
  stockOperationForm.targetQuantity = row.quantity
  stockOperationDialogVisible.value = true
}

async function submitStockOperation() {
  const drugId = stockOperationForm.drugId
  if (!drugId || drugId <= 0) {
    ElMessage.warning('请输入有效药品 ID')
    return
  }

  stockOperationSubmitting.value = true
  try {
    if (stockOperationMode.value === 'inbound') {
      const quantity = stockOperationForm.quantity
      if (!quantity || quantity <= 0) {
        ElMessage.warning('请输入大于 0 的入库数量')
        return
      }
      await http.post('/pharmacy/stocks/inbound', { drugId, quantity })
      ElMessage.success('入库完成')
    } else {
      const targetQuantity = stockOperationForm.targetQuantity
      if (targetQuantity == null || targetQuantity < 0) {
        ElMessage.warning('请输入不小于 0 的调整后库存')
        return
      }
      await http.post('/pharmacy/stocks/adjustment', { drugId, targetQuantity })
      ElMessage.success('库存调整完成')
    }
    stockOperationDialogVisible.value = false
    await refreshAll()
  } catch (error) {
    ElMessage.error(errorMessage(error, stockOperationMode.value === 'inbound' ? '入库失败' : '库存调整失败'))
  } finally {
    stockOperationSubmitting.value = false
  }
}

function resetTransactionPageAndLoad() {
  transactionPage.value = 1
  void loadTransactions()
}

async function loadTransactions() {
  transactionLoading.value = true
  try {
    const response = await http.get<ApiResponse<PageResult<StockTransaction>>>('/pharmacy/stock-transactions', {
      params: {
        drugId: transactionDrugId.value.trim() || undefined,
        prescriptionId: transactionPrescriptionId.value.trim() || undefined,
        page: transactionPage.value,
        size: transactionSize.value,
      },
    })
    transactions.value = response.data.data.items
    transactionTotal.value = response.data.data.total
  } catch (error) {
    ElMessage.error(errorMessage(error, '库存流水加载失败'))
  } finally {
    transactionLoading.value = false
  }
}

async function dispense(row: PharmacyPrescription) {
  try {
    await ElMessageBox.confirm(
      `确认发放处方 ${row.id} 的 ${row.drugNumber} 份药品？`,
      '确认发药',
      {
        confirmButtonText: '发药',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  dispensingId.value = row.id
  try {
    await http.post(`/pharmacy/prescriptions/${row.id}/dispense`)
    ElMessage.success('发药完成')
    await loadPrescriptions()
    await loadStocks()
    await loadTransactions()
  } catch (error) {
    ElMessage.error(errorMessage(error, '发药失败'))
  } finally {
    dispensingId.value = null
  }
}

async function returnPrescription(row: PharmacyPrescription) {
  try {
    await ElMessageBox.confirm(
      `确认退回处方 ${row.id} 的 ${row.drugNumber} 份药品？`,
      '确认退药',
      {
        confirmButtonText: '退药',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  returningId.value = row.id
  try {
    await http.post(`/pharmacy/prescriptions/${row.id}/return`)
    ElMessage.success('退药完成')
    await loadPrescriptions()
    await loadStocks()
    await loadTransactions()
  } catch (error) {
    ElMessage.error(errorMessage(error, '退药失败'))
  } finally {
    returningId.value = null
  }
}

function canDispense(row: PharmacyPrescription) {
  return row.state === 'PAID' && (row.stockQuantity ?? 0) >= row.drugNumber
}

function stateLabel(value: PrescriptionState) {
  return stateOptions.find((item) => item.value === value)?.label ?? value
}

function stateTagType(value: PrescriptionState) {
  if (value === 'PAID') return 'warning'
  if (value === 'DISPENSED') return 'success'
  if (value === 'RETURNED') return 'primary'
  if (value === 'REFUNDED' || value === 'CANCELLED') return 'danger'
  return 'info'
}

function transactionLabel(value: StockTransaction['transactionType']) {
  if (value === 'DISPENSE') return '发药'
  if (value === 'RETURN') return '退药'
  if (value === 'INBOUND') return '入库'
  if (value === 'ADJUST_INCREASE') return '调增'
  if (value === 'ADJUST_DECREASE') return '调减'
  return value
}

function transactionTagType(value: StockTransaction['transactionType']) {
  if (value === 'DISPENSE') return 'success'
  if (value === 'RETURN') return 'warning'
  if (value === 'INBOUND') return 'success'
  if (value === 'ADJUST_INCREASE') return 'primary'
  if (value === 'ADJUST_DECREASE') return 'danger'
  return 'info'
}

function stockType(row: PharmacyPrescription) {
  if (row.stockQuantity == null) return 'danger'
  return row.stockQuantity >= row.drugNumber ? 'success' : 'danger'
}

function stockAlertType(row: DrugStock) {
  if (row.quantity <= 0) return 'danger'
  if (stockMaxQuantity.value != null && row.quantity <= stockMaxQuantity.value) return 'warning'
  return 'success'
}

function parseNumericKeyword(value: string) {
  const text = value.trim()
  if (!/^\d+$/.test(text)) return undefined
  return Number(text)
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

function errorMessage(error: unknown, fallback: string) {
  return (error as ApiErrorPayload).response?.data?.message ?? fallback
}

onMounted(() => {
  void loadPrescriptions()
  void loadStocks()
  void loadTransactions()
})
</script>

<template>
  <section class="pharmacy-page">
    <div class="section-heading">
      <div>
        <p class="section-kicker">方向三 / 药房执行</p>
        <h2>药房发退药台</h2>
      </div>
      <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
    </div>

    <div class="pharmacy-toolbar">
      <el-input
        v-model="keyword"
        class="keyword-input"
        placeholder="输入处方号、挂号 ID 或药品 ID"
        clearable
        @keyup.enter="resetPageAndLoad"
        @clear="resetPageAndLoad"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="state" class="state-filter" @change="resetPageAndLoad">
        <el-option v-for="item in stateOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="resetPageAndLoad">查询</el-button>
    </div>

    <section class="pharmacy-panel">
      <div class="panel-heading pharmacy-panel-heading">
        <div>
          <p class="section-kicker">处方队列</p>
          <h3>{{ currentStateLabel }}处方</h3>
        </div>
        <el-tag effect="plain">共 {{ total }} 条</el-tag>
      </div>

      <el-table v-loading="loading" :data="prescriptions" table-layout="fixed" class="pharmacy-table">
        <el-table-column prop="id" label="处方号" width="100" />
        <el-table-column prop="registrationId" label="挂号 ID" width="110" />
        <el-table-column prop="drugId" label="药品 ID" width="100" />
        <el-table-column label="用药信息" min-width="240">
          <template #default="{ row }">
            <div class="prescription-main">
              <strong>{{ row.drugUsage }}</strong>
              <span>数量：{{ row.drugNumber }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="110">
          <template #default="{ row }">
            <el-tag :type="stockType(row)" effect="plain">{{ row.stockQuantity ?? 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="stateTagType(row.state)" effect="plain">{{ stateLabel(row.state) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开立时间" width="150">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <div class="action-row">
              <el-button
                v-if="row.state === 'PAID'"
                type="primary"
                size="small"
                :icon="Check"
                :loading="dispensingId === row.id"
                :disabled="!canDispense(row)"
                @click="dispense(row)"
              >
                发药
              </el-button>
              <el-button
                v-else-if="row.state === 'DISPENSED'"
                type="warning"
                size="small"
                :icon="Refresh"
                :loading="returningId === row.id"
                @click="returnPrescription(row)"
              >
                退药
              </el-button>
              <span v-else class="muted-action">无需操作</span>
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
          @current-change="loadPrescriptions"
        />
      </div>
    </section>

    <section class="pharmacy-panel stock-panel">
      <div class="panel-heading pharmacy-panel-heading">
        <div>
          <p class="section-kicker">库存预警</p>
          <h3>低库存药品</h3>
        </div>
        <el-tag effect="plain">共 {{ stockTotal }} 条</el-tag>
      </div>

      <div class="stock-toolbar">
        <el-input
          v-model="stockKeyword"
          class="filter-input"
          placeholder="药品 ID"
          clearable
          @keyup.enter="resetStockPageAndLoad"
          @clear="resetStockPageAndLoad"
        />
        <el-input-number
          v-model="stockMaxQuantity"
          class="threshold-input"
          :min="0"
          :max="999999"
          :controls="false"
          placeholder="预警阈值"
          @change="resetStockPageAndLoad"
        />
        <el-button type="primary" @click="resetStockPageAndLoad">查询库存</el-button>
        <el-button :icon="Plus" @click="openInboundDialog()">药品入库</el-button>
      </div>

      <el-table v-loading="stockLoading" :data="stocks" table-layout="fixed" class="pharmacy-table">
        <el-table-column prop="drugId" label="药品 ID" width="120" />
        <el-table-column label="当前库存" width="120">
          <template #default="{ row }">
            <el-tag :type="stockAlertType(row)" effect="plain">{{ row.quantity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="库存版本" width="110" />
        <el-table-column label="更新时间" min-width="150">
          <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-row">
              <el-button type="primary" size="small" :icon="Plus" @click="openInboundDialog(row)">入库</el-button>
              <el-button type="warning" size="small" :icon="EditPen" @click="openAdjustmentDialog(row)">调整</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="stockPage"
          v-model:page-size="stockSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="stockTotal"
          @size-change="resetStockPageAndLoad"
          @current-change="loadStocks"
        />
      </div>
    </section>

    <el-dialog v-model="stockOperationDialogVisible" :title="stockOperationTitle" width="460px">
      <el-form label-position="top">
        <el-form-item label="药品 ID" required>
          <el-input-number
            v-model="stockOperationForm.drugId"
            class="form-number-input"
            :min="1"
            :max="999999999"
            :controls="false"
            :disabled="stockOperationMode === 'adjust'"
          />
        </el-form-item>
        <el-form-item v-if="stockOperationMode === 'inbound'" label="入库数量" required>
          <el-input-number
            v-model="stockOperationForm.quantity"
            class="form-number-input"
            :min="1"
            :max="999999"
            :controls="false"
          />
        </el-form-item>
        <el-form-item v-else label="调整后库存" required>
          <el-input-number
            v-model="stockOperationForm.targetQuantity"
            class="form-number-input"
            :min="0"
            :max="999999"
            :controls="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockOperationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockOperationSubmitting" @click="submitStockOperation">
          {{ stockOperationMode === 'inbound' ? '确认入库' : '确认调整' }}
        </el-button>
      </template>
    </el-dialog>

    <section class="pharmacy-panel transaction-panel">
      <div class="panel-heading pharmacy-panel-heading">
        <div>
          <p class="section-kicker">库存流水</p>
          <h3>药品出入库记录</h3>
        </div>
        <el-tag effect="plain">共 {{ transactionTotal }} 条</el-tag>
      </div>

      <div class="transaction-toolbar">
        <el-input
          v-model="transactionDrugId"
          class="filter-input"
          placeholder="药品 ID"
          clearable
          @keyup.enter="resetTransactionPageAndLoad"
          @clear="resetTransactionPageAndLoad"
        />
        <el-input
          v-model="transactionPrescriptionId"
          class="filter-input"
          placeholder="处方号"
          clearable
          @keyup.enter="resetTransactionPageAndLoad"
          @clear="resetTransactionPageAndLoad"
        />
        <el-button type="primary" @click="resetTransactionPageAndLoad">查询流水</el-button>
      </div>

      <el-table v-loading="transactionLoading" :data="transactions" table-layout="fixed" class="pharmacy-table">
        <el-table-column prop="id" label="流水号" width="100" />
        <el-table-column prop="drugId" label="药品 ID" width="100" />
        <el-table-column prop="prescriptionId" label="处方号" width="110" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="transactionTagType(row.transactionType)" effect="plain">
              {{ transactionLabel(row.transactionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column label="库存变化" min-width="150">
          <template #default="{ row }">{{ row.quantityBefore }} -> {{ row.quantityAfter }}</template>
        </el-table-column>
        <el-table-column prop="operatorUserId" label="操作员 ID" width="110" />
        <el-table-column label="时间" width="150">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="transactionPage"
          v-model:page-size="transactionSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="transactionTotal"
          @size-change="resetTransactionPageAndLoad"
          @current-change="loadTransactions"
        />
      </div>
    </section>
  </section>
</template>

<style scoped>
.pharmacy-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.keyword-input {
  width: min(340px, 100%);
}

.state-filter {
  width: 150px;
}

.filter-input {
  width: 150px;
}

.pharmacy-panel {
  border: 1px solid var(--border);
  border-top: 3px solid #7c3aed;
  border-radius: 7px;
  background: var(--surface);
}

.transaction-panel {
  margin-top: 18px;
}

.stock-panel {
  margin-top: 18px;
}

.stock-toolbar,
.transaction-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #edf0f2;
}

.threshold-input {
  width: 150px;
}

.form-number-input {
  width: 100%;
}

.pharmacy-panel-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.pharmacy-table {
  --el-table-header-bg-color: #f8fafb;
}

.prescription-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.prescription-main strong {
  font-size: 13px;
  font-weight: 600;
  overflow-wrap: anywhere;
}

.prescription-main span,
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

.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px 16px;
  border-top: 1px solid #edf0f2;
}

@media (max-width: 720px) {
  .pharmacy-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .keyword-input,
  .state-filter,
  .filter-input {
    width: 100%;
  }

  .stock-toolbar,
  .transaction-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .pagination-row {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
