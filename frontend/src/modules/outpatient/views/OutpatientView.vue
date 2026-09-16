<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Check, DocumentChecked, FirstAidKit, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  acceptPatient,
  completeConsultation,
  getMedicalRecord,
  saveMedicalRecord,
  searchDiseases,
  searchPatients,
  type Disease,
  type MedicalRecordPayload,
  type Patient,
  type VisitState,
} from '../api'

const loading = ref(false)
const recordLoading = ref(false)
const saving = ref(false)
const selected = ref<Patient | null>(null)
const rows = ref<Patient[]>([])
const diseases = ref<Disease[]>([])
const total = ref(0)
const filters = reactive({ keyword: '', state: '' as VisitState | '', page: 1, size: 20 })
const emptyRecord = (): MedicalRecordPayload => ({
  chiefComplaint: '', presentIllness: '', presentTreatment: '', pastHistory: '', allergyHistory: '',
  physicalExamination: '', examinationProposal: '', precaution: '', diagnosis: '', treatmentPlan: '', diseaseIds: [],
})
const record = reactive<MedicalRecordPayload>(emptyRecord())

const canEdit = computed(() => selected.value?.state === 'IN_CONSULTATION')
const canComplete = computed(() => canEdit.value && record.diseaseIds.length > 0)

function apiMessage(error: unknown) {
  const response = error as { response?: { status?: number; data?: { message?: string } } }
  return response.response?.data?.message ?? '操作失败，请稍后重试'
}

function stateLabel(state: VisitState) {
  return { REGISTERED: '待诊', IN_CONSULTATION: '接诊中', COMPLETED: '已诊' }[state]
}

function stateType(state: VisitState) {
  return ({ REGISTERED: 'primary', IN_CONSULTATION: 'warning', COMPLETED: 'success' } as const)[state]
}

function ageText(patient: Patient) {
  if (patient.age == null) return '年龄未录入'
  return `${patient.age}${patient.ageType === 'DAY' ? '天' : '岁'}`
}

async function loadPatients() {
  loading.value = true
  try {
    const result = await searchPatients({
      keyword: filters.keyword || undefined,
      state: filters.state || undefined,
      page: filters.page,
      size: filters.size,
    })
    rows.value = result.items
    total.value = result.total
    if (selected.value) {
      selected.value = rows.value.find((item) => item.id === selected.value?.id) ?? null
    }
  } catch (error) {
    ElMessage.error(apiMessage(error))
  } finally {
    loading.value = false
  }
}

async function selectPatient(patient: Patient) {
  selected.value = patient
  Object.assign(record, emptyRecord())
  if (patient.state === 'REGISTERED') return
  recordLoading.value = true
  try {
    const saved = await getMedicalRecord(patient.id)
    Object.assign(record, saved, { diseaseIds: saved.diseases.map((item) => item.id) })
  } catch (error) {
    const response = error as { response?: { status?: number } }
    if (response.response?.status !== 404) ElMessage.error(apiMessage(error))
  } finally {
    recordLoading.value = false
  }
}

async function accept() {
  if (!selected.value) return
  try {
    selected.value = await acceptPatient(selected.value.id)
    ElMessage.success('已开始接诊')
    await loadPatients()
  } catch (error) {
    ElMessage.error(apiMessage(error))
  }
}

async function save() {
  if (!selected.value || !canEdit.value) return false
  saving.value = true
  try {
    const saved = await saveMedicalRecord(selected.value.id, record)
    Object.assign(record, saved, { diseaseIds: saved.diseases.map((item) => item.id) })
    ElMessage.success('病历已保存')
    return true
  } catch (error) {
    ElMessage.error(apiMessage(error))
    return false
  } finally {
    saving.value = false
  }
}

async function complete() {
  if (!selected.value || !canComplete.value) return
  await ElMessageBox.confirm(`确认完成 ${selected.value.realName} 的本次看诊？`, '完成看诊', { type: 'warning' })
  try {
    if (!await save()) return
    selected.value = await completeConsultation(selected.value.id)
    ElMessage.success('本次看诊已完成')
    await loadPatients()
  } catch (error) {
    ElMessage.error(apiMessage(error))
  }
}

onMounted(async () => {
  const [, diseaseOptions] = await Promise.all([loadPatients(), searchDiseases().catch(() => [])])
  diseases.value = diseaseOptions
})
</script>

<template>
  <section class="outpatient-page">
    <div class="section-heading">
      <div>
        <p class="section-kicker">门诊业务</p>
        <h2>医生工作台</h2>
      </div>
      <el-tooltip content="刷新患者队列" placement="top">
        <button class="icon-button" type="button" aria-label="刷新患者队列" @click="loadPatients"><el-icon><Refresh /></el-icon></button>
      </el-tooltip>
    </div>

    <div class="outpatient-layout">
      <aside class="patient-panel">
        <div class="queue-toolbar">
          <el-input v-model="filters.keyword" clearable placeholder="病历号、姓名或证件号" @keyup.enter="filters.page = 1; loadPatients()">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filters.state" clearable placeholder="全部状态" @change="filters.page = 1; loadPatients()">
            <el-option label="待诊" value="REGISTERED" />
            <el-option label="接诊中" value="IN_CONSULTATION" />
            <el-option label="已诊" value="COMPLETED" />
          </el-select>
        </div>
        <div v-loading="loading" class="patient-list">
          <button v-for="patient in rows" :key="patient.id" type="button" class="patient-row" :class="{ active: selected?.id === patient.id }" @click="selectPatient(patient)">
            <span class="patient-row-main"><strong>{{ patient.realName }}</strong><small>{{ patient.caseNumber }}</small></span>
            <span class="patient-row-meta"><el-tag :type="stateType(patient.state)" effect="plain" size="small">{{ stateLabel(patient.state) }}</el-tag><small>{{ patient.noon === 'AM' ? '上午' : '下午' }}</small></span>
          </button>
          <el-empty v-if="!loading && rows.length === 0" description="暂无患者" :image-size="72" />
        </div>
        <el-pagination v-model:current-page="filters.page" :page-size="filters.size" :total="total" small layout="prev, pager, next" @current-change="loadPatients" />
      </aside>

      <div class="consultation-panel">
        <el-empty v-if="!selected" description="请选择患者" :image-size="96" />
        <template v-else>
          <header class="patient-summary">
            <div>
              <div class="patient-name"><h3>{{ selected.realName }}</h3><el-tag :type="stateType(selected.state)" effect="plain">{{ stateLabel(selected.state) }}</el-tag></div>
              <p>{{ selected.gender === 'MALE' ? '男' : selected.gender === 'FEMALE' ? '女' : '未知' }} · {{ ageText(selected) }} · {{ selected.caseNumber }}</p>
              <p>{{ selected.departmentName }} · {{ selected.employeeName }} · {{ selected.registrationLevelName }}</p>
            </div>
            <el-button v-if="selected.state === 'REGISTERED'" type="primary" :icon="FirstAidKit" @click="accept">接诊</el-button>
            <div v-else-if="selected.state === 'IN_CONSULTATION'" class="record-actions">
              <el-button :icon="DocumentChecked" :loading="saving" @click="save">保存病历</el-button>
              <el-button type="success" :icon="Check" :disabled="!canComplete" @click="complete">完成看诊</el-button>
            </div>
          </header>

          <el-form v-loading="recordLoading" :model="record" label-position="top" class="medical-record-form" :disabled="!canEdit">
            <div class="form-grid">
              <el-form-item label="主诉" class="span-two"><el-input v-model="record.chiefComplaint" maxlength="500" show-word-limit /></el-form-item>
              <el-form-item label="现病史"><el-input v-model="record.presentIllness" type="textarea" :rows="4" /></el-form-item>
              <el-form-item label="现病治疗情况"><el-input v-model="record.presentTreatment" type="textarea" :rows="4" /></el-form-item>
              <el-form-item label="既往史"><el-input v-model="record.pastHistory" type="textarea" :rows="3" /></el-form-item>
              <el-form-item label="过敏史"><el-input v-model="record.allergyHistory" type="textarea" :rows="3" /></el-form-item>
              <el-form-item label="体格检查"><el-input v-model="record.physicalExamination" type="textarea" :rows="3" /></el-form-item>
              <el-form-item label="检查建议"><el-input v-model="record.examinationProposal" type="textarea" :rows="3" /></el-form-item>
              <el-form-item label="最终诊断" class="span-two">
                <el-select v-model="record.diseaseIds" multiple filterable placeholder="请选择疾病诊断">
                  <el-option v-for="item in diseases" :key="item.id" :label="`${item.code} ${item.name}`" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="诊断说明"><el-input v-model="record.diagnosis" type="textarea" :rows="3" /></el-form-item>
              <el-form-item label="治疗方案"><el-input v-model="record.treatmentPlan" type="textarea" :rows="3" /></el-form-item>
              <el-form-item label="注意事项" class="span-two"><el-input v-model="record.precaution" type="textarea" :rows="2" /></el-form-item>
            </div>
          </el-form>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
.outpatient-layout { display: grid; grid-template-columns: minmax(280px, 340px) minmax(0, 1fr); min-height: 620px; border: 1px solid var(--border); border-radius: 7px; background: var(--surface); overflow: hidden; }
.patient-panel { display: flex; flex-direction: column; min-width: 0; border-right: 1px solid var(--border); }
.queue-toolbar { display: grid; gap: 10px; padding: 14px; border-bottom: 1px solid var(--border); }
.patient-list { flex: 1; min-height: 320px; }
.patient-row { width: 100%; min-height: 72px; display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 12px 14px; border: 0; border-bottom: 1px solid var(--border); background: transparent; color: inherit; text-align: left; cursor: pointer; }
.patient-row:hover, .patient-row.active { background: #f2f7f6; }
.patient-row.active { box-shadow: inset 3px 0 var(--primary); }
.patient-row-main, .patient-row-meta { display: flex; flex-direction: column; gap: 5px; min-width: 0; }
.patient-row-main small, .patient-row-meta small { color: var(--muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.patient-row-meta { align-items: flex-end; flex: 0 0 auto; }
.patient-panel :deep(.el-pagination) { justify-content: center; min-height: 52px; border-top: 1px solid var(--border); }
.consultation-panel { min-width: 0; }
.consultation-panel > .el-empty { min-height: 620px; }
.patient-summary { min-height: 108px; display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 18px 22px; border-bottom: 1px solid var(--border); background: #f8faf9; }
.patient-name { display: flex; align-items: center; gap: 10px; }
.patient-name h3 { margin: 0; font-size: 20px; }
.patient-summary p { margin: 6px 0 0; color: var(--muted); font-size: 13px; }
.record-actions { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; }
.medical-record-form { padding: 20px 22px 8px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; }
.span-two { grid-column: 1 / -1; }
.form-grid :deep(.el-select) { width: 100%; }
@media (max-width: 900px) {
  .outpatient-layout { grid-template-columns: 1fr; }
  .patient-panel { border-right: 0; border-bottom: 1px solid var(--border); }
  .patient-list { min-height: 0; max-height: 290px; overflow-y: auto; }
  .consultation-panel > .el-empty { min-height: 360px; }
}
@media (max-width: 640px) {
  .patient-summary { align-items: flex-start; flex-direction: column; }
  .record-actions { width: 100%; justify-content: stretch; }
  .record-actions :deep(.el-button) { flex: 1; margin-left: 0; }
  .form-grid { grid-template-columns: 1fr; }
  .span-two { grid-column: auto; }
  .medical-record-form { padding: 16px 14px 4px; }
}
</style>
