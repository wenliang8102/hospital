<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Check, Edit, Plus, Refresh, Search, Switch, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { http, type ApiResponse } from '@/core/http'

interface Role {
  code: string
  name: string
}

interface EmployeeOption {
  id: number
  realName: string
  departmentName: string
}

interface UserAccount {
  id: number
  username: string
  displayName: string
  employeeId: number | null
  employeeName?: string
  enabled: boolean
  roles: Role[]
}

interface PageData<T> {
  items: T[]
  page: number
  size: number
  total: number
}

interface UserForm {
  id: number | null
  username: string
  password: string
  displayName: string
  employeeId: number | ''
  enabled: boolean
  roleCodes: string[]
}

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const users = ref<UserAccount[]>([])
const roles = ref<Role[]>([])
const employees = ref<EmployeeOption[]>([])
const total = ref(0)

const filters = reactive({
  keyword: '',
  enabled: '' as '' | 'true' | 'false',
})

const pagination = reactive({
  page: 1,
  size: 20,
})

const form = reactive<UserForm>({
  id: null,
  username: '',
  password: '',
  displayName: '',
  employeeId: '',
  enabled: true,
  roleCodes: [],
})

const rules = computed<FormRules<UserForm>>(() => ({
  username: form.id ? [] : [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { max: 64, message: '用户名不能超过 64 个字符', trigger: 'blur' },
  ],
  password: form.id ? [] : [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 5, max: 128, message: '密码长度为 5 到 128 个字符', trigger: 'blur' },
  ],
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' },
    { max: 64, message: '显示名称不能超过 64 个字符', trigger: 'blur' },
  ],
  roleCodes: [{ required: true, type: 'array', min: 1, message: '请选择至少一个角色', trigger: 'change' }],
}))

const dialogTitle = computed(() => (form.id ? '编辑账号' : '新增账号'))

function enabledParam() {
  if (filters.enabled === '') return undefined
  return filters.enabled === 'true'
}

async function loadRoles() {
  const response = await http.get<ApiResponse<Role[]>>('/platform/roles')
  roles.value = response.data.data
}

async function loadEmployees() {
  const response = await http.get<ApiResponse<PageData<EmployeeOption>>>('/master-data/employees/manage', {
    params: { active: true, page: 1, size: 100 },
  })
  employees.value = response.data.data.items
}

async function loadUsers() {
  loading.value = true
  try {
    const response = await http.get<ApiResponse<PageData<UserAccount>>>('/platform/users', {
      params: {
        keyword: filters.keyword.trim() || undefined,
        enabled: enabledParam(),
        page: pagination.page,
        size: pagination.size,
      },
    })
    users.value = response.data.data.items
    total.value = response.data.data.total
  } finally {
    loading.value = false
  }
}

function search() {
  pagination.page = 1
  void loadUsers()
}

function resetFilters() {
  filters.keyword = ''
  filters.enabled = ''
  pagination.page = 1
  void loadUsers()
}

function openCreateDialog() {
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    displayName: '',
    employeeId: '',
    enabled: true,
    roleCodes: [],
  })
  dialogVisible.value = true
}

function openEditDialog(row: UserAccount) {
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    displayName: row.displayName,
    employeeId: row.employeeId ?? '',
    enabled: row.enabled,
    roleCodes: row.roles.map((role) => role.code),
  })
  dialogVisible.value = true
}

async function saveUser() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  saving.value = true
  try {
    if (form.id) {
      await http.put<ApiResponse<UserAccount>>(`/platform/users/${form.id}`, {
        displayName: form.displayName.trim(),
        employeeId: form.employeeId || null,
        enabled: form.enabled,
        roleCodes: form.roleCodes,
      })
      ElMessage.success('账号已更新')
    } else {
      await http.post<ApiResponse<UserAccount>>('/platform/users', {
        username: form.username.trim(),
        password: form.password,
        displayName: form.displayName.trim(),
        employeeId: form.employeeId || null,
        enabled: form.enabled,
        roleCodes: form.roleCodes,
      })
      ElMessage.success('账号已创建')
    }
    dialogVisible.value = false
    await loadUsers()
  } finally {
    saving.value = false
  }
}

async function changeEnabled(row: UserAccount) {
  const nextEnabled = !row.enabled
  const label = nextEnabled ? '启用' : '停用'
  await ElMessageBox.confirm(`确认${label}账号“${row.username}”？`, '账号状态变更', {
    type: 'warning',
    confirmButtonText: label,
    cancelButtonText: '取消',
  })
  await http.patch<ApiResponse<UserAccount>>(`/platform/users/${row.id}/enabled`, { enabled: nextEnabled })
  ElMessage.success(`账号已${label}`)
  await loadUsers()
}

function handlePageChange(page: number) {
  pagination.page = page
  void loadUsers()
}

function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  void loadUsers()
}

function roleText(row: UserAccount) {
  return row.roles.map((role) => role.name).join(' / ') || '-'
}

onMounted(() => {
  void Promise.all([loadRoles(), loadEmployees(), loadUsers()])
})
</script>

<template>
  <section class="platform-users-page">
    <div class="section-heading">
      <div>
        <p class="section-kicker">平台管理</p>
        <h2>账号与角色</h2>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增账号</el-button>
    </div>

    <div class="platform-toolbar">
      <el-input v-model="filters.keyword" class="keyword-input" placeholder="用户名、显示名或员工姓名" clearable @keyup.enter="search">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filters.enabled" class="filter-select" placeholder="状态" clearable>
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

    <div class="account-table">
      <el-table v-loading="loading" :data="users" row-key="id">
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="displayName" label="显示名称" min-width="150" />
        <el-table-column label="绑定员工" min-width="140">
          <template #default="{ row }">
            {{ row.employeeName ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="220">
          <template #default="{ row }">
            {{ roleText(row) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" effect="plain">
              {{ row.enabled ? '已启用' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-tooltip content="编辑" placement="top">
                <el-button :icon="Edit" circle @click="openEditDialog(row)" />
              </el-tooltip>
              <el-tooltip :content="row.enabled ? '停用' : '启用'" placement="top">
                <el-button :type="row.enabled ? 'warning' : 'success'" :icon="row.enabled ? Warning : Switch" circle @click="changeEnabled(row)" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="Boolean(form.id)" maxlength="64" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" maxlength="128" show-password />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" maxlength="64" />
        </el-form-item>
        <el-form-item label="绑定员工">
          <el-select v-model="form.employeeId" class="full-width" clearable>
            <el-option v-for="item in employees" :key="item.id" :label="`${item.realName} / ${item.departmentName}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleCodes">
          <el-checkbox-group v-model="form.roleCodes">
            <el-checkbox v-for="role in roles" :key="role.code" :label="role.code">{{ role.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="form.enabled" active-text="已启用" inactive-text="已停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :icon="Check" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.platform-users-page {
  min-width: 0;
}

.platform-toolbar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(120px, 150px) auto;
  gap: 12px;
  margin-bottom: 14px;
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

.account-table {
  overflow: hidden;
  border: 1px solid var(--border);
  border-top: 3px solid var(--primary);
  border-radius: 7px;
  background: var(--surface);
}

.account-table :deep(.el-table) {
  --el-table-header-bg-color: #f8fafb;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
  border-top: 1px solid var(--border);
}

@media (max-width: 780px) {
  .platform-toolbar {
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
