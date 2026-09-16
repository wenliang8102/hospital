<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Check, Edit, Plus, Refresh, Search, Switch, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { http, type ApiResponse } from '@/core/http'
import { useAuthStore } from '@/stores/auth'

type DepartmentType = 'OUTPATIENT' | 'CHECK' | 'INSPECTION' | 'DISPOSAL' | 'FINANCE' | 'PHARMACY'
type MasterDataTab =
  | 'departments'
  | 'employees'
  | 'regist-levels'
  | 'settle-categories'
  | 'diseases'
  | 'medical-technologies'
  | 'drugs'
  | 'scheduling'
type MaintainableTab = 'diseases' | 'medical-technologies' | 'drugs' | 'scheduling'

interface Department {
  id: number
  code: string
  name: string
  type: DepartmentType
  active: boolean
}

interface PageData<T> {
  items: T[]
  page: number
  size: number
  total: number
}

interface DepartmentForm {
  id: number | null
  code: string
  name: string
  type: DepartmentType | ''
  active: boolean
}

interface EmployeeForm {
  id: number | null
  realName: string
  departmentId: number | ''
  active: boolean
}

interface MaintenanceForm {
  id: number | null
  code: string
  name: string
  icd: string
  category: string
  format: string
  unit: string
  manufacturer: string
  dosage: string
  type: string
  price: number | null
  mnemonicCode: string
  priceType: string
  weekRule: string
  departmentId: number | ''
  active: boolean
}

interface LookupRow {
  id: number
  active?: boolean
  [key: string]: string | number | boolean | undefined
}

interface LookupColumn {
  prop: string
  label: string
  minWidth?: number
  width?: number
  formatter?: (row: LookupRow) => string
}

interface LookupConfig {
  name: Exclude<MasterDataTab, 'departments'>
  label: string
  endpoint: string
  paged: boolean
  maintainable?: boolean
  keywordPlaceholder: string
  columns: LookupColumn[]
}

const departmentTypes: Array<{ value: DepartmentType; label: string }> = [
  { value: 'OUTPATIENT', label: '门诊' },
  { value: 'CHECK', label: '检查' },
  { value: 'INSPECTION', label: '检验' },
  { value: 'DISPOSAL', label: '处置' },
  { value: 'FINANCE', label: '财务' },
  { value: 'PHARMACY', label: '药房' },
]

const lookupConfigs: LookupConfig[] = [
  {
    name: 'employees',
    label: '员工',
    endpoint: '/master-data/employees/manage',
    paged: true,
    keywordPlaceholder: '员工或科室名称',
    columns: [
      { prop: 'realName', label: '姓名', minWidth: 140 },
      { prop: 'departmentName', label: '所属科室', minWidth: 150 },
      { prop: 'registLevelName', label: '挂号级别', minWidth: 130, formatter: (row) => String(row.registLevelName ?? '-') },
      { prop: 'active', label: '状态', width: 110, formatter: activeLabel },
    ],
  },
  {
    name: 'regist-levels',
    label: '挂号级别',
    endpoint: '/master-data/regist-levels',
    paged: true,
    keywordPlaceholder: '编码或名称',
    columns: [
      { prop: 'code', label: '级别编码', minWidth: 150 },
      { prop: 'name', label: '级别名称', minWidth: 160 },
      { prop: 'active', label: '状态', width: 110, formatter: activeLabel },
    ],
  },
  {
    name: 'settle-categories',
    label: '结算类别',
    endpoint: '/master-data/settle-categories',
    paged: true,
    keywordPlaceholder: '编码或名称',
    columns: [
      { prop: 'code', label: '类别编码', minWidth: 150 },
      { prop: 'name', label: '类别名称', minWidth: 160 },
      { prop: 'active', label: '状态', width: 110, formatter: activeLabel },
    ],
  },
  {
    name: 'diseases',
    label: '疾病',
    endpoint: '/master-data/diseases/manage',
    paged: true,
    maintainable: true,
    keywordPlaceholder: '编码、名称或 ICD',
    columns: [
      { prop: 'code', label: '疾病编码', minWidth: 140 },
      { prop: 'name', label: '疾病名称', minWidth: 190 },
      { prop: 'icd', label: 'ICD', minWidth: 120, formatter: fallbackValue('icd') },
      { prop: 'category', label: '疾病分类', minWidth: 130, formatter: fallbackValue('category') },
      { prop: 'active', label: '状态', width: 110, formatter: activeLabel },
    ],
  },
  {
    name: 'medical-technologies',
    label: '医技项目',
    endpoint: '/master-data/medical-technologies/manage',
    paged: true,
    maintainable: true,
    keywordPlaceholder: '项目编码或名称',
    columns: [
      { prop: 'code', label: '项目编码', minWidth: 140 },
      { prop: 'name', label: '项目名称', minWidth: 180 },
      { prop: 'type', label: '项目类型', minWidth: 120 },
      { prop: 'price', label: '价格', minWidth: 110, formatter: moneyValue('price') },
      { prop: 'departmentName', label: '执行科室', minWidth: 140 },
      { prop: 'active', label: '状态', width: 110, formatter: activeLabel },
    ],
  },
  {
    name: 'drugs',
    label: '药品',
    endpoint: '/master-data/drugs/manage',
    paged: true,
    maintainable: true,
    keywordPlaceholder: '药品编码、名称或助记码',
    columns: [
      { prop: 'code', label: '药品编码', minWidth: 140 },
      { prop: 'name', label: '药品名称', minWidth: 180 },
      { prop: 'format', label: '规格', minWidth: 140 },
      { prop: 'unit', label: '单位', width: 90 },
      { prop: 'price', label: '价格', minWidth: 110, formatter: moneyValue('price') },
      { prop: 'mnemonicCode', label: '助记码', minWidth: 120, formatter: fallbackValue('mnemonicCode') },
      { prop: 'active', label: '状态', width: 110, formatter: activeLabel },
    ],
  },
  {
    name: 'scheduling',
    label: '排班',
    endpoint: '/master-data/scheduling/manage',
    paged: true,
    maintainable: true,
    keywordPlaceholder: '排班名称或周规则',
    columns: [
      { prop: 'name', label: '排班名称', minWidth: 180 },
      { prop: 'weekRule', label: '周规则', minWidth: 170 },
      { prop: 'active', label: '状态', width: 110, formatter: activeLabel },
    ],
  },
]

const loading = ref(false)
const lookupLoading = ref(false)
const saving = ref(false)
const employeeSaving = ref(false)
const maintenanceSaving = ref(false)
const dialogVisible = ref(false)
const employeeDialogVisible = ref(false)
const maintenanceDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const employeeFormRef = ref<FormInstance>()
const maintenanceFormRef = ref<FormInstance>()
const activeTab = ref<MasterDataTab>('departments')
const auth = useAuthStore()

const departments = ref<Department[]>([])
const total = ref(0)
const lookupRows = ref<LookupRow[]>([])
const lookupTotal = ref(0)

const filters = reactive({
  keyword: '',
  type: '' as DepartmentType | '',
  active: '' as '' | 'true' | 'false',
})

const pagination = reactive({
  page: 1,
  size: 20,
})

const lookupFilters = reactive({
  keyword: '',
  departmentId: '' as number | '',
  active: '' as '' | 'true' | 'false',
})

const lookupPagination = reactive({
  page: 1,
  size: 20,
})

const form = reactive<DepartmentForm>({
  id: null,
  code: '',
  name: '',
  type: '',
  active: true,
})

const employeeForm = reactive<EmployeeForm>({
  id: null,
  realName: '',
  departmentId: '',
  active: true,
})

const maintenanceForm = reactive<MaintenanceForm>({
  id: null,
  code: '',
  name: '',
  icd: '',
  category: '',
  format: '',
  unit: '',
  manufacturer: '',
  dosage: '',
  type: '',
  price: 0,
  mnemonicCode: '',
  priceType: '',
  weekRule: '',
  departmentId: '',
  active: true,
})

const rules: FormRules<DepartmentForm> = {
  code: [
    { required: true, message: '请输入科室编码', trigger: 'blur' },
    { max: 64, message: '科室编码不能超过 64 个字符', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入科室名称', trigger: 'blur' },
    { max: 64, message: '科室名称不能超过 64 个字符', trigger: 'blur' },
  ],
  type: [{ required: true, message: '请选择科室类型', trigger: 'change' }],
}

const employeeRules: FormRules<EmployeeForm> = {
  realName: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' },
    { max: 64, message: '员工姓名不能超过 64 个字符', trigger: 'blur' },
  ],
  departmentId: [{ required: true, message: '请选择所属科室', trigger: 'change' }],
}

const maintenanceRules: FormRules<MaintenanceForm> = {
  code: [
    { required: true, message: '请输入编码', trigger: 'blur' },
    { max: 64, message: '编码不能超过 64 个字符', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: 255, message: '名称不能超过 255 个字符', trigger: 'blur' },
  ],
  format: [{ required: true, message: '请输入规格', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  type: [{ required: true, message: '请输入类型', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择执行科室', trigger: 'change' }],
  weekRule: [
    { required: true, message: '请输入周规则', trigger: 'blur' },
    { pattern: /^[01]{14}$/, message: '周规则必须是 14 位 0/1', trigger: 'blur' },
  ],
}

const dialogTitle = computed(() => (form.id ? '编辑科室' : '新增科室'))
const employeeDialogTitle = computed(() => (employeeForm.id ? '编辑员工' : '新增员工'))
const canWriteMasterData = computed(() => auth.hasPermission('master-data:write'))
const visibleLookupConfigs = computed(() => lookupConfigs.filter((item) => canWriteMasterData.value || item.name !== 'scheduling'))
const activeLookupConfig = computed(() => visibleLookupConfigs.value.find((item) => item.name === activeTab.value))
const activeMaintenanceConfig = computed(() => {
  const config = activeLookupConfig.value
  return config?.maintainable && canWriteMasterData.value ? config : undefined
})
const pageTitle = computed(() => activeLookupConfig.value?.label ?? '科室管理')
const maintenanceDialogTitle = computed(() => `${maintenanceForm.id ? '编辑' : '新增'}${activeMaintenanceConfig.value?.label ?? '基础资料'}`)

function typeLabel(type: DepartmentType) {
  return departmentTypes.find((item) => item.value === type)?.label ?? type
}

function activeParam() {
  if (filters.active === '') return undefined
  return filters.active === 'true'
}

async function loadDepartments() {
  loading.value = true
  try {
    const response = await http.get<ApiResponse<PageData<Department>>>('/master-data/departments/manage', {
      params: {
        keyword: filters.keyword.trim() || undefined,
        type: filters.type || undefined,
        active: activeParam(),
        page: pagination.page,
        size: pagination.size,
      },
    })
    departments.value = response.data.data.items
    total.value = response.data.data.total
  } finally {
    loading.value = false
  }
}

async function loadLookup() {
  const config = activeLookupConfig.value
  if (!config) return

  lookupLoading.value = true
  try {
    const params: Record<string, string | number | boolean | undefined> = {
      keyword: lookupFilters.keyword.trim() || undefined,
    }
    if (activeTab.value === 'employees') {
      params.departmentId = lookupFilters.departmentId || undefined
    }
    if (canWriteMasterData.value && (activeTab.value === 'employees' || config.maintainable)) {
      params.active = lookupFilters.active === '' ? undefined : lookupFilters.active === 'true'
    }
    if (config.paged) {
      params.page = lookupPagination.page
      params.size = lookupPagination.size
    }

    const response = await http.get<ApiResponse<PageData<LookupRow> | LookupRow[]>>(lookupEndpoint(config), { params })
    if (Array.isArray(response.data.data)) {
      lookupRows.value = response.data.data
      lookupTotal.value = response.data.data.length
    } else {
      lookupRows.value = response.data.data.items
      lookupTotal.value = response.data.data.total
    }
  } finally {
    lookupLoading.value = false
  }
}

function search() {
  pagination.page = 1
  void loadDepartments()
}

function searchLookup() {
  lookupPagination.page = 1
  void loadLookup()
}

function resetFilters() {
  filters.keyword = ''
  filters.type = ''
  filters.active = ''
  pagination.page = 1
  void loadDepartments()
}

function resetLookupFilters() {
  lookupFilters.keyword = ''
  lookupFilters.departmentId = ''
  lookupFilters.active = ''
  lookupPagination.page = 1
  void loadLookup()
}

function openCreateDialog() {
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    type: '',
    active: true,
  })
  dialogVisible.value = true
}

function openEditDialog(row: Department) {
  Object.assign(form, row)
  dialogVisible.value = true
}

function openCreateEmployeeDialog() {
  Object.assign(employeeForm, {
    id: null,
    realName: '',
    departmentId: '',
    active: true,
  })
  employeeDialogVisible.value = true
}

function openEditEmployeeDialog(row: LookupRow) {
  Object.assign(employeeForm, {
    id: row.id,
    realName: String(row.realName ?? ''),
    departmentId: Number(row.departmentId),
    active: row.active !== false,
  })
  employeeDialogVisible.value = true
}

function openCreateMaintenanceDialog() {
  Object.assign(maintenanceForm, emptyMaintenanceForm())
  maintenanceDialogVisible.value = true
}

function openEditMaintenanceDialog(row: LookupRow) {
  Object.assign(maintenanceForm, {
    ...emptyMaintenanceForm(),
    id: row.id,
    code: String(row.code ?? ''),
    name: String(row.name ?? ''),
    icd: String(row.icd ?? ''),
    category: String(row.category ?? ''),
    format: String(row.format ?? ''),
    unit: String(row.unit ?? ''),
    manufacturer: String(row.manufacturer ?? ''),
    dosage: String(row.dosage ?? ''),
    type: String(row.type ?? ''),
    price: typeof row.price === 'number' ? row.price : Number(row.price ?? 0),
    mnemonicCode: String(row.mnemonicCode ?? ''),
    priceType: String(row.priceType ?? ''),
    weekRule: String(row.weekRule ?? ''),
    departmentId: typeof row.departmentId === 'number' ? row.departmentId : '',
    active: row.active !== false,
  })
  maintenanceDialogVisible.value = true
}

async function saveDepartment() {
  const valid = await formRef.value?.validate()
  if (!valid || !form.type) return

  const payload = {
    code: form.code.trim(),
    name: form.name.trim(),
    type: form.type,
    active: form.active,
  }

  saving.value = true
  try {
    if (form.id) {
      await http.put<ApiResponse<Department>>(`/master-data/departments/manage/${form.id}`, payload)
      ElMessage.success('科室已更新')
    } else {
      await http.post<ApiResponse<Department>>('/master-data/departments/manage', payload)
      ElMessage.success('科室已创建')
    }
    dialogVisible.value = false
    await loadDepartments()
  } finally {
    saving.value = false
  }
}

async function saveEmployee() {
  const valid = await employeeFormRef.value?.validate()
  if (!valid || !employeeForm.departmentId) return

  const payload = {
    realName: employeeForm.realName.trim(),
    departmentId: employeeForm.departmentId,
    registLevelId: null,
    schedulingId: null,
    active: employeeForm.active,
  }

  employeeSaving.value = true
  try {
    if (employeeForm.id) {
      await http.put<ApiResponse<LookupRow>>(`/master-data/employees/manage/${employeeForm.id}`, payload)
      ElMessage.success('员工已更新')
    } else {
      await http.post<ApiResponse<LookupRow>>('/master-data/employees/manage', payload)
      ElMessage.success('员工已创建')
    }
    employeeDialogVisible.value = false
    await loadLookup()
  } finally {
    employeeSaving.value = false
  }
}

async function saveMaintenance() {
  const config = activeMaintenanceConfig.value
  if (!config) return

  const valid = await maintenanceFormRef.value?.validate?.()
  if (valid === false) return

  const payload = maintenancePayload(config.name as MaintainableTab)
  if (!payload) return

  maintenanceSaving.value = true
  try {
    if (maintenanceForm.id) {
      await http.put<ApiResponse<LookupRow>>(`${config.endpoint}/${maintenanceForm.id}`, payload)
      ElMessage.success(`${config.label}已更新`)
    } else {
      await http.post<ApiResponse<LookupRow>>(config.endpoint, payload)
      ElMessage.success(`${config.label}已创建`)
    }
    maintenanceDialogVisible.value = false
    await loadLookup()
  } finally {
    maintenanceSaving.value = false
  }
}

async function changeActive(row: Department) {
  const nextActive = !row.active
  const label = nextActive ? '启用' : '停用'
  await ElMessageBox.confirm(`确认${label}科室“${row.name}”？`, '科室状态变更', {
    type: 'warning',
    confirmButtonText: label,
    cancelButtonText: '取消',
  })
  await http.patch<ApiResponse<Department>>(`/master-data/departments/manage/${row.id}/active`, { active: nextActive })
  ElMessage.success(`科室已${label}`)
  await loadDepartments()
}

async function changeEmployeeActive(row: LookupRow) {
  const nextActive = row.active === false
  const label = nextActive ? '启用' : '停用'
  await ElMessageBox.confirm(`确认${label}员工“${row.realName}”？`, '员工状态变更', {
    type: 'warning',
    confirmButtonText: label,
    cancelButtonText: '取消',
  })
  await http.patch<ApiResponse<LookupRow>>(`/master-data/employees/manage/${row.id}/active`, { active: nextActive })
  ElMessage.success(`员工已${label}`)
  await loadLookup()
}

async function changeMaintenanceActive(row: LookupRow) {
  const config = activeMaintenanceConfig.value
  if (!config) return

  const nextActive = row.active === false
  const label = nextActive ? '启用' : '停用'
  await ElMessageBox.confirm(`确认${label}${config.label}“${row.name}”？`, `${config.label}状态变更`, {
    type: 'warning',
    confirmButtonText: label,
    cancelButtonText: '取消',
  })
  await http.patch<ApiResponse<LookupRow>>(`${config.endpoint}/${row.id}/active`, { active: nextActive })
  ElMessage.success(`${config.label}已${label}`)
  await loadLookup()
}

function handlePageChange(page: number) {
  pagination.page = page
  void loadDepartments()
}

function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  void loadDepartments()
}

function handleLookupPageChange(page: number) {
  lookupPagination.page = page
  void loadLookup()
}

function handleLookupSizeChange(size: number) {
  lookupPagination.size = size
  lookupPagination.page = 1
  void loadLookup()
}

function handleTabChange(name: string | number) {
  if (name === 'departments') {
    void loadDepartments()
    return
  }
  lookupPagination.page = 1
  void loadLookup()
}

function activeLabel(row: LookupRow) {
  return row.active === false ? '已停用' : '已启用'
}

function fallbackValue(prop: string) {
  return (row: LookupRow) => String(row[prop] ?? '-')
}

function moneyValue(prop: string) {
  return (row: LookupRow) => {
    const value = row[prop]
    if (typeof value !== 'number') return '-'
    return value.toFixed(2)
  }
}

function cellValue(row: LookupRow, column: LookupColumn) {
  return column.formatter ? column.formatter(row) : String(row[column.prop] ?? '-')
}

function lookupEndpoint(config: LookupConfig) {
  if (canWriteMasterData.value) return config.endpoint
  if (config.name === 'employees') return '/master-data/employees'
  return `/master-data/${config.name}`
}

function emptyMaintenanceForm(): MaintenanceForm {
  return {
    id: null,
    code: '',
    name: '',
    icd: '',
    category: '',
    format: '',
    unit: '',
    manufacturer: '',
    dosage: '',
    type: '',
    price: 0,
    mnemonicCode: '',
    priceType: '',
    weekRule: '',
    departmentId: '',
    active: true,
  }
}

function optionalText(value: string) {
  const text = value.trim()
  return text || null
}

function maintenancePayload(name: MaintainableTab) {
  if (name === 'diseases') {
    return {
      code: maintenanceForm.code.trim(),
      name: maintenanceForm.name.trim(),
      icd: optionalText(maintenanceForm.icd),
      category: optionalText(maintenanceForm.category),
      active: maintenanceForm.active,
    }
  }

  if (name === 'drugs') {
    return {
      code: maintenanceForm.code.trim(),
      name: maintenanceForm.name.trim(),
      format: maintenanceForm.format.trim(),
      unit: maintenanceForm.unit.trim(),
      manufacturer: optionalText(maintenanceForm.manufacturer),
      dosage: optionalText(maintenanceForm.dosage),
      type: optionalText(maintenanceForm.type),
      price: Number(maintenanceForm.price ?? 0),
      mnemonicCode: optionalText(maintenanceForm.mnemonicCode),
      active: maintenanceForm.active,
    }
  }

  if (name === 'scheduling') {
    return {
      name: maintenanceForm.name.trim(),
      weekRule: maintenanceForm.weekRule.trim(),
      active: maintenanceForm.active,
    }
  }

  if (!maintenanceForm.departmentId) return null
  return {
    code: maintenanceForm.code.trim(),
    name: maintenanceForm.name.trim(),
    format: optionalText(maintenanceForm.format),
    price: Number(maintenanceForm.price ?? 0),
    type: maintenanceForm.type.trim(),
    priceType: optionalText(maintenanceForm.priceType),
    departmentId: maintenanceForm.departmentId,
    active: maintenanceForm.active,
  }
}

onMounted(() => {
  void loadDepartments()
})

defineExpose({
  maintenanceForm,
  openCreateMaintenanceDialog,
  saveMaintenance,
})
</script>

<template>
  <section class="master-data-page">
    <div class="section-heading">
      <div>
        <p class="section-kicker">基础数据</p>
        <h2>{{ pageTitle }}</h2>
      </div>
      <el-button v-if="canWriteMasterData && activeTab === 'departments'" type="primary" :icon="Plus" @click="openCreateDialog">新增科室</el-button>
      <el-button v-if="canWriteMasterData && activeTab === 'employees'" type="primary" :icon="Plus" @click="openCreateEmployeeDialog">新增员工</el-button>
      <el-button v-if="activeMaintenanceConfig" type="primary" :icon="Plus" @click="openCreateMaintenanceDialog">新增{{ activeMaintenanceConfig.label }}</el-button>
    </div>

    <el-tabs v-model="activeTab" class="master-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="科室管理" name="departments">
        <template v-if="activeTab === 'departments'">
          <div class="master-toolbar department-toolbar">
            <el-input v-model="filters.keyword" class="keyword-input" placeholder="科室编码或名称" clearable @keyup.enter="search">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="filters.type" class="filter-select" placeholder="科室类型" clearable>
              <el-option v-for="item in departmentTypes" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="filters.active" class="filter-select" placeholder="状态" clearable>
              <el-option label="已启用" value="true" />
              <el-option label="已停用" value="false" />
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

          <div class="data-table">
            <el-table v-loading="loading" :data="departments" row-key="id">
              <el-table-column prop="code" label="科室编码" min-width="140" />
              <el-table-column prop="name" label="科室名称" min-width="160" />
              <el-table-column label="科室类型" min-width="120">
                <template #default="{ row }">
                  {{ typeLabel(row.type) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="row.active ? 'success' : 'info'" effect="plain">
                    {{ row.active ? '已启用' : '已停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column v-if="canWriteMasterData" label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <div class="row-actions">
                    <el-tooltip content="编辑" placement="top">
                      <el-button :icon="Edit" circle @click="openEditDialog(row)" />
                    </el-tooltip>
                    <el-tooltip :content="row.active ? '停用' : '启用'" placement="top">
                      <el-button :type="row.active ? 'warning' : 'success'" :icon="row.active ? Warning : Switch" circle @click="changeActive(row)" />
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
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
        </template>
      </el-tab-pane>

      <el-tab-pane v-for="config in visibleLookupConfigs" :key="config.name" :label="config.label" :name="config.name">
        <template v-if="activeTab === config.name">
          <div class="master-toolbar lookup-toolbar" :class="{ 'employee-toolbar': config.name === 'employees' || config.maintainable }">
            <el-input v-model="lookupFilters.keyword" class="keyword-input" :placeholder="config.keywordPlaceholder" clearable @keyup.enter="searchLookup">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-if="config.name === 'employees'" v-model="lookupFilters.departmentId" class="filter-select" placeholder="所属科室" clearable>
              <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-select v-if="canWriteMasterData && (config.name === 'employees' || config.maintainable)" v-model="lookupFilters.active" class="filter-select" placeholder="状态" clearable>
              <el-option label="已启用" value="true" />
              <el-option label="已停用" value="false" />
            </el-select>
            <div class="toolbar-actions">
              <el-tooltip content="查询" placement="top">
                <el-button type="primary" :icon="Search" circle @click="searchLookup" />
              </el-tooltip>
              <el-tooltip content="重置" placement="top">
                <el-button :icon="Refresh" circle @click="resetLookupFilters" />
              </el-tooltip>
            </div>
          </div>

          <div class="data-table">
            <el-table v-loading="lookupLoading" :data="lookupRows" row-key="id">
              <el-table-column v-for="column in config.columns" :key="column.prop" :label="column.label" :min-width="column.minWidth" :width="column.width">
                <template #default="{ row }">
                  <el-tag v-if="column.prop === 'active'" :type="row.active ? 'success' : 'info'" effect="plain">
                    {{ cellValue(row, column) }}
                  </el-tag>
                  <span v-else>{{ cellValue(row, column) }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="canWriteMasterData && (config.name === 'employees' || config.maintainable)" label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <div class="row-actions">
                    <el-tooltip content="编辑" placement="top">
                      <el-button :icon="Edit" circle @click="config.name === 'employees' ? openEditEmployeeDialog(row) : openEditMaintenanceDialog(row)" />
                    </el-tooltip>
                    <el-tooltip :content="row.active ? '停用' : '启用'" placement="top">
                      <el-button
                        :type="row.active ? 'warning' : 'success'"
                        :icon="row.active ? Warning : Switch"
                        circle
                        @click="config.name === 'employees' ? changeEmployeeActive(row) : changeMaintenanceActive(row)"
                      />
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="config.paged" class="pagination-bar">
              <el-pagination
                background
                layout="total, sizes, prev, pager, next"
                :current-page="lookupPagination.page"
                :page-size="lookupPagination.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="lookupTotal"
                @current-change="handleLookupPageChange"
                @size-change="handleLookupSizeChange"
              />
            </div>
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="科室编码" prop="code">
          <el-input v-model="form.code" maxlength="64" />
        </el-form-item>
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="科室类型" prop="type">
          <el-select v-model="form.type" class="full-width">
            <el-option v-for="item in departmentTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="form.active" active-text="已启用" inactive-text="已停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :icon="Check" @click="saveDepartment">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="employeeDialogVisible" :title="employeeDialogTitle" width="520px" destroy-on-close>
      <el-form ref="employeeFormRef" :model="employeeForm" :rules="employeeRules" label-width="88px">
        <el-form-item label="员工姓名" prop="realName">
          <el-input v-model="employeeForm.realName" maxlength="64" />
        </el-form-item>
        <el-form-item label="所属科室" prop="departmentId">
          <el-select v-model="employeeForm.departmentId" class="full-width">
            <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="employeeForm.active" active-text="已启用" inactive-text="已停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="employeeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="employeeSaving" :icon="Check" @click="saveEmployee">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="maintenanceDialogVisible" :title="maintenanceDialogTitle" width="620px" destroy-on-close>
      <el-form ref="maintenanceFormRef" :model="maintenanceForm" :rules="maintenanceRules" label-width="88px">
        <div class="maintenance-form-grid">
          <el-form-item v-if="activeTab !== 'scheduling'" label="编码" prop="code">
            <el-input v-model="maintenanceForm.code" maxlength="64" />
          </el-form-item>
          <el-form-item label="名称" prop="name">
            <el-input v-model="maintenanceForm.name" maxlength="255" />
          </el-form-item>

          <template v-if="activeTab === 'diseases'">
            <el-form-item label="ICD">
              <el-input v-model="maintenanceForm.icd" maxlength="50" />
            </el-form-item>
            <el-form-item label="疾病分类">
              <el-input v-model="maintenanceForm.category" maxlength="50" />
            </el-form-item>
          </template>

          <template v-if="activeTab === 'drugs'">
            <el-form-item label="规格" prop="format">
              <el-input v-model="maintenanceForm.format" maxlength="255" />
            </el-form-item>
            <el-form-item label="单位" prop="unit">
              <el-input v-model="maintenanceForm.unit" maxlength="16" />
            </el-form-item>
            <el-form-item label="生产厂家">
              <el-input v-model="maintenanceForm.manufacturer" maxlength="255" />
            </el-form-item>
            <el-form-item label="用法" prop="dosage">
              <el-input v-model="maintenanceForm.dosage" maxlength="64" />
            </el-form-item>
            <el-form-item label="药品类型" prop="type">
              <el-input v-model="maintenanceForm.type" maxlength="64" />
            </el-form-item>
            <el-form-item label="价格" prop="price">
              <el-input v-model.number="maintenanceForm.price" type="number" min="0" />
            </el-form-item>
            <el-form-item label="助记码">
              <el-input v-model="maintenanceForm.mnemonicCode" maxlength="64" />
            </el-form-item>
          </template>

          <template v-if="activeTab === 'medical-technologies'">
            <el-form-item label="规格">
              <el-input v-model="maintenanceForm.format" maxlength="64" />
            </el-form-item>
            <el-form-item label="项目类型" prop="type">
              <el-input v-model="maintenanceForm.type" maxlength="32" />
            </el-form-item>
            <el-form-item label="价格" prop="price">
              <el-input v-model.number="maintenanceForm.price" type="number" min="0" />
            </el-form-item>
            <el-form-item label="费用类型">
              <el-input v-model="maintenanceForm.priceType" maxlength="64" />
            </el-form-item>
            <el-form-item label="执行科室" prop="departmentId">
              <el-select v-model="maintenanceForm.departmentId" class="full-width">
                <el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </template>

          <template v-if="activeTab === 'scheduling'">
            <el-form-item label="周规则" prop="weekRule">
              <el-input v-model="maintenanceForm.weekRule" maxlength="14" />
            </el-form-item>
          </template>

          <el-form-item label="启用状态">
            <el-switch v-model="maintenanceForm.active" active-text="已启用" inactive-text="已停用" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="maintenanceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="maintenanceSaving" :icon="Check" @click="saveMaintenance">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.master-data-page {
  min-width: 0;
}

.master-tabs {
  min-width: 0;
}

.master-toolbar {
  display: grid;
  gap: 12px;
  margin-bottom: 14px;
}

.department-toolbar {
  grid-template-columns: minmax(220px, 1fr) minmax(150px, 180px) minmax(120px, 150px) auto;
}

.lookup-toolbar {
  grid-template-columns: minmax(220px, 1fr) auto;
}

.employee-toolbar {
  grid-template-columns: minmax(220px, 1fr) minmax(150px, 180px) minmax(120px, 150px) auto;
}

.keyword-input,
.filter-select,
.full-width {
  width: 100%;
}

.toolbar-actions,
.row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.data-table {
  overflow: hidden;
  border: 1px solid var(--border);
  border-top: 3px solid var(--primary);
  border-radius: 7px;
  background: var(--surface);
}

.data-table :deep(.el-table) {
  --el-table-header-bg-color: #f8fafb;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid var(--border);
}

.maintenance-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 14px;
}

@media (max-width: 780px) {
  .department-toolbar,
  .lookup-toolbar,
  .employee-toolbar {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    justify-content: flex-end;
  }

  .pagination-bar {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .maintenance-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
