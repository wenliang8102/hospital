<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { CirclePlus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  cancelRegistration,
  createRegistration,
  fetchEmployees,
  fetchRegistrationOptions,
  generateCaseNumber,
  searchRegistrations,
  type CreateRegistrationRequest,
  type DepartmentOption,
  type EmployeeOption,
  type Registration,
  type RegistrationLevelOption,
  type SettlementCategoryOption,
} from '../api'

const loading = ref(false)
const submitting = ref(false)
const dialogOpen = ref(false)
const detailOpen = ref(false)
const formRef = ref<FormInstance>()
const rows = ref<Registration[]>([])
const selected = ref<Registration | null>(null)
const total = ref(0)
const filters = reactive({ keyword: '', state: '', page: 1, size: 20 })
const departments = ref<DepartmentOption[]>([])
const employees = ref<EmployeeOption[]>([])
const levels = ref<RegistrationLevelOption[]>([])
const settlementCategories = ref<SettlementCategoryOption[]>([])

type RegistrationForm = Omit<CreateRegistrationRequest,
  'departmentId' | 'employeeId' | 'registrationLevelId' | 'settlementCategoryId'> & {
  departmentId?: number
  employeeId?: number
  registrationLevelId?: number
  settlementCategoryId?: number
}

const emptyForm = (): RegistrationForm => ({
  requestId: crypto.randomUUID(),
  caseNumber: '',
  realName: '',
  gender: 'UNKNOWN',
  age: undefined,
  ageType: 'YEAR',
  visitDate: tomorrow(),
  noon: 'AM',
  departmentId: undefined,
  employeeId: undefined,
  registrationLevelId: undefined,
  settlementCategoryId: undefined,
  booked: false,
  registrationMethod: 'CASH',
})
const form = reactive<RegistrationForm>(emptyForm())

const rules: FormRules<RegistrationForm> = {
  caseNumber: [{ required: true, message: '请生成病历号', trigger: 'change' }],
  realName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  visitDate: [{ required: true, message: '请选择就诊日期', trigger: 'change' }],
  departmentId: [{ required: true, type: 'number', min: 1, message: '请选择科室', trigger: 'change' }],
  employeeId: [{ required: true, type: 'number', min: 1, message: '请选择医生', trigger: 'change' }],
  registrationLevelId: [{ required: true, type: 'number', min: 1, message: '请选择挂号级别', trigger: 'change' }],
  settlementCategoryId: [{ required: true, type: 'number', min: 1, message: '请选择结算类别', trigger: 'change' }],
}

const currentDoctor = computed(() => employees.value.find((item) => item.id === form.employeeId))
const registrationFee = computed(() => currentDoctor.value?.registrationFee ?? 0)

function tomorrow() {
  const date = new Date()
  date.setDate(date.getDate() + 1)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}T00:00:00`
}

function disabledDate(date: Date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

function apiMessage(error: unknown) {
  const response = error as { response?: { data?: { message?: string } } }
  return response.response?.data?.message ?? '操作失败，请稍后重试'
}

async function loadRows() {
  loading.value = true
  try {
    const result = await searchRegistrations({
      keyword: filters.keyword || undefined,
      state: filters.state || undefined,
      page: filters.page,
      size: filters.size,
    })
    rows.value = result.items
    total.value = result.total
  } catch (error) {
    ElMessage.error(apiMessage(error))
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const options = await fetchRegistrationOptions()
    departments.value = options.departments
    levels.value = options.levels
    settlementCategories.value = options.settlementCategories
  } catch (error) {
    ElMessage.error(apiMessage(error))
  }
}

async function openCreate() {
  Object.assign(form, emptyForm())
  employees.value = []
  dialogOpen.value = true
  await formRef.value?.clearValidate()
  try {
    form.caseNumber = await generateCaseNumber()
  } catch (error) {
    ElMessage.error(apiMessage(error))
  }
}

async function departmentChanged() {
  form.employeeId = undefined
  form.registrationLevelId = undefined
  employees.value = form.departmentId ? await fetchEmployees(form.departmentId) : []
}

function doctorChanged() {
  const doctor = currentDoctor.value
  form.registrationLevelId = doctor?.registrationLevelId
}

async function submit() {
  if (!await formRef.value?.validate().catch(() => false)) return
  submitting.value = true
  try {
    await createRegistration({
      ...form,
      departmentId: form.departmentId!,
      employeeId: form.employeeId!,
      registrationLevelId: form.registrationLevelId!,
      settlementCategoryId: form.settlementCategoryId!,
    })
    ElMessage.success('挂号成功')
    dialogOpen.value = false
    filters.page = 1
    await loadRows()
  } catch (error) {
    ElMessage.error(apiMessage(error))
  } finally {
    submitting.value = false
  }
}

function showDetail(row: Registration) {
  selected.value = row
  detailOpen.value = true
}

async function cancel(row: Registration) {
  await ElMessageBox.confirm(`确认退掉 ${row.realName} 的本次挂号？`, '退号确认', { type: 'warning' })
  try {
    await cancelRegistration(row.id)
    ElMessage.success('退号成功')
    await loadRows()
  } catch (error) {
    ElMessage.error(apiMessage(error))
  }
}

function stateLabel(state: Registration['state']) {
  return { REGISTERED: '待诊', IN_CONSULTATION: '接诊中', COMPLETED: '已诊', CANCELLED: '已退号' }[state]
}

function stateType(state: Registration['state']) {
  return ({ REGISTERED: 'primary', IN_CONSULTATION: 'warning', COMPLETED: 'success', CANCELLED: 'info' } as const)[state]
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadRows()])
})
</script>

<template>
  <section class="registration-page">
    <div class="section-heading">
      <div>
        <p class="section-kicker">门诊业务</p>
        <h2>挂号工作台</h2>
      </div>
      <el-button type="primary" :icon="CirclePlus" @click="openCreate">新建挂号</el-button>
    </div>

    <div class="registration-toolbar">
      <el-input v-model="filters.keyword" class="registration-search" clearable placeholder="病历号、姓名或证件号" @keyup.enter="filters.page = 1; loadRows()">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filters.state" class="state-filter" clearable placeholder="全部状态" @change="filters.page = 1; loadRows()">
        <el-option label="待诊" value="REGISTERED" />
        <el-option label="接诊中" value="IN_CONSULTATION" />
        <el-option label="已诊" value="COMPLETED" />
        <el-option label="已退号" value="CANCELLED" />
      </el-select>
      <el-button :icon="Search" @click="filters.page = 1; loadRows()">查询</el-button>
      <el-tooltip content="刷新" placement="top">
        <button class="icon-button" type="button" aria-label="刷新" @click="loadRows"><el-icon><Refresh /></el-icon></button>
      </el-tooltip>
    </div>

    <div class="registration-table">
      <el-table v-loading="loading" :data="rows" row-key="id" @row-dblclick="showDetail">
        <el-table-column prop="caseNumber" label="病历号" min-width="160" />
        <el-table-column prop="realName" label="患者" width="100" />
        <el-table-column prop="departmentName" label="科室" width="110" />
        <el-table-column prop="employeeName" label="医生" width="110" />
        <el-table-column label="就诊日期" min-width="150">
          <template #default="{ row }">{{ row.visitDate.slice(0, 10) }} {{ row.noon === 'AM' ? '上午' : '下午' }}</template>
        </el-table-column>
        <el-table-column prop="registrationLevelName" label="号别" width="90" />
        <el-table-column label="费用" width="90"><template #default="{ row }">¥{{ Number(row.registrationFee).toFixed(2) }}</template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="stateType(row.state)" effect="plain">{{ stateLabel(row.state) }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.state === 'REGISTERED'" link type="danger" @click="cancel(row)">退号</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无挂号记录" :image-size="80" /></template>
      </el-table>
      <div class="registration-pagination">
        <el-pagination v-model:current-page="filters.page" v-model:page-size="filters.size" :total="total" layout="total, prev, pager, next" @current-change="loadRows" />
      </div>
    </div>

    <el-dialog v-model="dialogOpen" title="新建挂号" width="min(860px, 94vw)" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <div class="registration-form-grid">
          <el-form-item label="病历号" prop="caseNumber"><el-input v-model="form.caseNumber" readonly /></el-form-item>
          <el-form-item label="患者姓名" prop="realName"><el-input v-model="form.realName" maxlength="64" /></el-form-item>
          <el-form-item label="性别" prop="gender"><el-segmented v-model="form.gender" :options="[{ label: '男', value: 'MALE' }, { label: '女', value: 'FEMALE' }, { label: '未知', value: 'UNKNOWN' }]" /></el-form-item>
          <el-form-item label="证件号"><el-input v-model="form.cardNumber" maxlength="32" /></el-form-item>
          <el-form-item label="年龄"><el-input-number v-model="form.age" :min="0" :max="150" controls-position="right" /></el-form-item>
          <el-form-item label="年龄单位"><el-select v-model="form.ageType"><el-option label="岁" value="YEAR" /><el-option label="天" value="DAY" /></el-select></el-form-item>
          <el-form-item label="就诊日期" prop="visitDate"><el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DDT00:00:00" :disabled-date="disabledDate" /></el-form-item>
          <el-form-item label="午别"><el-segmented v-model="form.noon" :options="[{ label: '上午', value: 'AM' }, { label: '下午', value: 'PM' }]" /></el-form-item>
          <el-form-item label="科室" prop="departmentId"><el-select v-model="form.departmentId" filterable placeholder="请选择科室" @change="departmentChanged"><el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
          <el-form-item label="医生" prop="employeeId"><el-select v-model="form.employeeId" filterable placeholder="请选择医生" :disabled="!form.departmentId" @change="doctorChanged"><el-option v-for="item in employees" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
          <el-form-item label="挂号级别" prop="registrationLevelId"><el-select v-model="form.registrationLevelId" placeholder="选择医生后自动带出" disabled><el-option v-for="item in levels" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
          <el-form-item label="结算类别" prop="settlementCategoryId"><el-select v-model="form.settlementCategoryId" placeholder="请选择结算类别"><el-option v-for="item in settlementCategories" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
          <el-form-item label="挂号方式"><el-select v-model="form.registrationMethod"><el-option label="现金" value="CASH" /><el-option label="银行卡" value="BANK_CARD" /><el-option label="微信" value="WECHAT" /><el-option label="支付宝" value="ALIPAY" /><el-option label="医保" value="MEDICAL_INSURANCE" /></el-select></el-form-item>
          <el-form-item label="家庭住址" class="form-span-two"><el-input v-model="form.homeAddress" maxlength="255" /></el-form-item>
        </div>
        <div class="registration-summary"><span>应收挂号费</span><strong>¥{{ registrationFee.toFixed(2) }}</strong></div>
      </el-form>
      <template #footer><el-button @click="dialogOpen = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">确认挂号</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailOpen" title="挂号详情" size="min(460px, 92vw)">
      <el-descriptions v-if="selected" :column="1" border>
        <el-descriptions-item label="病历号">{{ selected.caseNumber }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ selected.realName }}</el-descriptions-item>
        <el-descriptions-item label="就诊">{{ selected.visitDate.slice(0, 10) }} {{ selected.noon === 'AM' ? '上午' : '下午' }}</el-descriptions-item>
        <el-descriptions-item label="科室 / 医生">{{ selected.departmentName }} / {{ selected.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="号别">{{ selected.registrationLevelName }}</el-descriptions-item>
        <el-descriptions-item label="结算类别">{{ selected.settlementCategoryName }}</el-descriptions-item>
        <el-descriptions-item label="费用">¥{{ Number(selected.registrationFee).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="stateType(selected.state)" effect="plain">{{ stateLabel(selected.state) }}</el-tag></el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </section>
</template>

<style scoped>
.registration-toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.registration-search { width: min(340px, 100%); }
.state-filter { width: 150px; }
.registration-table { overflow: hidden; border: 1px solid var(--border); border-radius: 7px; background: var(--surface); }
.registration-pagination { display: flex; justify-content: flex-end; padding: 14px 16px; border-top: 1px solid var(--border); }
.registration-form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; }
.registration-form-grid :deep(.el-select), .registration-form-grid :deep(.el-date-editor), .registration-form-grid :deep(.el-input-number) { width: 100%; }
.form-span-two { grid-column: 1 / -1; }
.registration-summary { display: flex; align-items: center; justify-content: space-between; padding: 14px 16px; background: #f3f7f6; border-left: 3px solid var(--primary); }
.registration-summary span { color: var(--muted); font-size: 13px; }
.registration-summary strong { font-size: 22px; color: var(--primary-dark); }
@media (max-width: 700px) {
  .registration-toolbar { align-items: stretch; flex-wrap: wrap; }
  .registration-search { width: 100%; }
  .state-filter { flex: 1; }
  .registration-form-grid { grid-template-columns: 1fr; }
  .form-span-two { grid-column: auto; }
  .registration-pagination { overflow-x: auto; justify-content: flex-start; }
}
</style>
