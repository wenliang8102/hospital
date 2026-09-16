<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Check, CirclePlus, Delete, DocumentChecked, FirstAidKit, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  acceptPatient,
  cancelMedicalOrder,
  cancelPrescription,
  completeConsultation,
  createMedicalOrder,
  createPrescription,
  getMedicalOrders,
  getMedicalRecord,
  getPrescriptions,
  saveMedicalRecord,
  searchDiseases,
  searchDrugs,
  searchMedicalTechnologies,
  searchPatients,
  type Disease,
  type MedicalRecordPayload,
  type MedicalOrder,
  type MedicalOrderType,
  type Patient,
  type Prescription,
  type Drug,
  type MedicalTechnology,
  type VisitState,
} from '../api'

const loading = ref(false)
const recordLoading = ref(false)
const saving = ref(false)
const selected = ref<Patient | null>(null)
const rows = ref<Patient[]>([])
const diseases = ref<Disease[]>([])
const technologies = ref<MedicalTechnology[]>([])
const drugs = ref<Drug[]>([])
const medicalOrders = ref<MedicalOrder[]>([])
const prescriptions = ref<Prescription[]>([])
const orderDialogOpen = ref(false)
const prescriptionDialogOpen = ref(false)
const orderSubmitting = ref(false)
const prescriptionSubmitting = ref(false)
const total = ref(0)
const filters = reactive({ keyword: '', state: '' as VisitState | '', page: 1, size: 20 })
const emptyRecord = (): MedicalRecordPayload => ({
  chiefComplaint: '', presentIllness: '', presentTreatment: '', pastHistory: '', allergyHistory: '',
  physicalExamination: '', examinationProposal: '', precaution: '', diagnosis: '', treatmentPlan: '', diseaseIds: [],
})
const record = reactive<MedicalRecordPayload>(emptyRecord())
const orderForm = reactive<{
  type: MedicalOrderType
  medicalTechnologyId?: number
  requestInfo: string
  bodyPosition: string
  remark: string
}>({ type: 'CHECK', medicalTechnologyId: undefined, requestInfo: '', bodyPosition: '', remark: '' })
const prescriptionForm = reactive<{
  drugId?: number
  drugUsage: string
  drugNumber: number
}>({ drugId: undefined, drugUsage: '', drugNumber: 1 })

const canEdit = computed(() => selected.value?.state === 'IN_CONSULTATION')
const canComplete = computed(() => canEdit.value && record.diseaseIds.length > 0)
const filteredTechnologies = computed(() => technologies.value.filter((item) => item.type === orderForm.type))

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

function orderStateLabel(state: string) {
  return {
    CREATED: '待缴费', PAID: '已缴费', ACCEPTED: '已接收', EXECUTED: '已执行',
    RESULT_REPORTED: '已报告', CANCELLED: '已作废', REFUNDED: '已退费',
  }[state] ?? state
}

function orderTypeLabel(type: MedicalOrderType) {
  return { CHECK: '检查', INSPECTION: '检验', DISPOSAL: '处置' }[type]
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
  medicalOrders.value = []
  prescriptions.value = []
  if (patient.state === 'REGISTERED') return
  recordLoading.value = true
  try {
    const [savedRecord, savedOrders, savedPrescriptions] = await Promise.allSettled([
      getMedicalRecord(patient.id), getMedicalOrders(patient.id), getPrescriptions(patient.id),
    ])
    if (savedRecord.status === 'fulfilled') {
      Object.assign(record, savedRecord.value, { diseaseIds: savedRecord.value.diseases.map((item) => item.id) })
    } else {
      const response = savedRecord.reason as { response?: { status?: number } }
      if (response.response?.status !== 404) ElMessage.error(apiMessage(savedRecord.reason))
    }
    if (savedOrders.status === 'fulfilled') medicalOrders.value = savedOrders.value
    if (savedPrescriptions.status === 'fulfilled') prescriptions.value = savedPrescriptions.value
  } catch (error) {
    ElMessage.error(apiMessage(error))
  } finally {
    recordLoading.value = false
  }
}

function openOrderDialog() {
  Object.assign(orderForm, { type: 'CHECK', medicalTechnologyId: undefined, requestInfo: '', bodyPosition: '', remark: '' })
  orderDialogOpen.value = true
}

function openPrescriptionDialog() {
  Object.assign(prescriptionForm, { drugId: undefined, drugUsage: '', drugNumber: 1 })
  prescriptionDialogOpen.value = true
}

async function submitOrder() {
  if (!selected.value || !orderForm.medicalTechnologyId) return
  orderSubmitting.value = true
  try {
    medicalOrders.value = await createMedicalOrder(selected.value.id, {
      type: orderForm.type,
      medicalTechnologyId: orderForm.medicalTechnologyId,
      requestInfo: orderForm.requestInfo || undefined,
      bodyPosition: orderForm.bodyPosition || undefined,
      remark: orderForm.remark || undefined,
    })
    orderDialogOpen.value = false
    ElMessage.success('医技申请已开立')
  } catch (error) {
    ElMessage.error(apiMessage(error))
  } finally {
    orderSubmitting.value = false
  }
}

async function submitPrescription() {
  if (!selected.value || !prescriptionForm.drugId || !prescriptionForm.drugUsage.trim()) return
  prescriptionSubmitting.value = true
  try {
    prescriptions.value = await createPrescription(selected.value.id, {
      drugId: prescriptionForm.drugId,
      drugUsage: prescriptionForm.drugUsage,
      drugNumber: prescriptionForm.drugNumber,
    })
    prescriptionDialogOpen.value = false
    ElMessage.success('处方已开立')
  } catch (error) {
    ElMessage.error(apiMessage(error))
  } finally {
    prescriptionSubmitting.value = false
  }
}

async function removeOrder(order: MedicalOrder) {
  if (!selected.value) return
  await ElMessageBox.confirm(`确认作废 ${order.itemName}？`, '作废医技申请', { type: 'warning' })
  try {
    await cancelMedicalOrder(selected.value.id, order)
    medicalOrders.value = await getMedicalOrders(selected.value.id)
    ElMessage.success('医技申请已作废')
  } catch (error) {
    ElMessage.error(apiMessage(error))
  }
}

async function removePrescription(item: Prescription) {
  if (!selected.value) return
  await ElMessageBox.confirm(`确认作废 ${item.drugName}？`, '作废处方', { type: 'warning' })
  try {
    await cancelPrescription(selected.value.id, item.id)
    prescriptions.value = await getPrescriptions(selected.value.id)
    ElMessage.success('处方已作废')
  } catch (error) {
    ElMessage.error(apiMessage(error))
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
  const [, diseaseOptions, technologyOptions, drugOptions] = await Promise.all([
    loadPatients(), searchDiseases().catch(() => []), searchMedicalTechnologies().catch(() => []),
    searchDrugs().catch(() => []),
  ])
  diseases.value = diseaseOptions
  technologies.value = technologyOptions
  drugs.value = drugOptions
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
              <el-button :icon="CirclePlus" @click="openOrderDialog">开医技</el-button>
              <el-button :icon="CirclePlus" @click="openPrescriptionDialog">开处方</el-button>
              <el-button :icon="DocumentChecked" :loading="saving" @click="save">保存病历</el-button>
              <el-button type="success" :icon="Check" :disabled="!canComplete" @click="complete">完成看诊</el-button>
            </div>
          </header>

          <div v-if="selected.state !== 'REGISTERED'" class="clinical-order-grid">
            <section class="clinical-order-section">
              <h4>医技申请 <span>{{ medicalOrders.length }}</span></h4>
              <el-table :data="medicalOrders" size="small" max-height="190" empty-text="暂无医技申请">
                <el-table-column label="类型" width="64"><template #default="{ row }">{{ orderTypeLabel(row.type) }}</template></el-table-column>
                <el-table-column prop="itemName" label="项目" min-width="120" show-overflow-tooltip />
                <el-table-column label="费用" width="76"><template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template></el-table-column>
                <el-table-column label="状态" width="78"><template #default="{ row }"><el-tag size="small" effect="plain">{{ orderStateLabel(row.state) }}</el-tag></template></el-table-column>
                <el-table-column v-if="canEdit" label="" width="42">
                  <template #default="{ row }"><el-button v-if="row.state === 'CREATED'" link type="danger" :icon="Delete" aria-label="作废医技申请" @click="removeOrder(row)" /></template>
                </el-table-column>
              </el-table>
            </section>
            <section class="clinical-order-section">
              <h4>处方 <span>{{ prescriptions.length }}</span></h4>
              <el-table :data="prescriptions" size="small" max-height="190" empty-text="暂无处方">
                <el-table-column prop="drugName" label="药品" min-width="110" show-overflow-tooltip />
                <el-table-column label="数量" width="72"><template #default="{ row }">{{ row.drugNumber }}{{ row.drugUnit }}</template></el-table-column>
                <el-table-column label="金额" width="76"><template #default="{ row }">¥{{ Number(row.totalAmount).toFixed(2) }}</template></el-table-column>
                <el-table-column label="状态" width="78"><template #default="{ row }"><el-tag size="small" effect="plain">{{ orderStateLabel(row.state) }}</el-tag></template></el-table-column>
                <el-table-column v-if="canEdit" label="" width="42">
                  <template #default="{ row }"><el-button v-if="row.state === 'CREATED'" link type="danger" :icon="Delete" aria-label="作废处方" @click="removePrescription(row)" /></template>
                </el-table-column>
              </el-table>
            </section>
          </div>

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

    <el-dialog v-model="orderDialogOpen" title="开立医技申请" width="min(600px, 94vw)">
      <el-form :model="orderForm" label-position="top" @submit.prevent="submitOrder">
        <el-form-item label="申请类型">
          <el-segmented v-model="orderForm.type" :options="[{ label: '检查', value: 'CHECK' }, { label: '检验', value: 'INSPECTION' }, { label: '处置', value: 'DISPOSAL' }]" @change="orderForm.medicalTechnologyId = undefined" />
        </el-form-item>
        <el-form-item label="医技项目" required>
          <el-select v-model="orderForm.medicalTechnologyId" filterable placeholder="请选择医技项目">
            <el-option v-for="item in filteredTechnologies" :key="item.id" :label="`${item.name} · ¥${Number(item.price).toFixed(2)}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <div class="dialog-form-grid">
          <el-form-item label="申请目的"><el-input v-model="orderForm.requestInfo" maxlength="1000" /></el-form-item>
          <el-form-item label="部位"><el-input v-model="orderForm.bodyPosition" maxlength="255" /></el-form-item>
        </div>
        <el-form-item label="备注"><el-input v-model="orderForm.remark" type="textarea" :rows="3" maxlength="1000" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="orderDialogOpen = false">取消</el-button><el-button type="primary" :loading="orderSubmitting" :disabled="!orderForm.medicalTechnologyId" @click="submitOrder">确认开立</el-button></template>
    </el-dialog>

    <el-dialog v-model="prescriptionDialogOpen" title="开立处方" width="min(600px, 94vw)">
      <el-form :model="prescriptionForm" label-position="top" @submit.prevent="submitPrescription">
        <el-form-item label="药品" required>
          <el-select v-model="prescriptionForm.drugId" filterable placeholder="请选择药品">
            <el-option v-for="item in drugs" :key="item.id" :label="`${item.name} ${item.format} · ¥${Number(item.price).toFixed(2)}/${item.unit}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <div class="dialog-form-grid">
          <el-form-item label="用法" required><el-input v-model="prescriptionForm.drugUsage" maxlength="500" /></el-form-item>
          <el-form-item label="数量" required><el-input-number v-model="prescriptionForm.drugNumber" :min="1" :max="10000" controls-position="right" /></el-form-item>
        </div>
      </el-form>
      <template #footer><el-button @click="prescriptionDialogOpen = false">取消</el-button><el-button type="primary" :loading="prescriptionSubmitting" :disabled="!prescriptionForm.drugId || !prescriptionForm.drugUsage.trim()" @click="submitPrescription">确认开立</el-button></template>
    </el-dialog>
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
.clinical-order-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); border-bottom: 1px solid var(--border); }
.clinical-order-section { min-width: 0; padding: 14px 18px; }
.clinical-order-section + .clinical-order-section { border-left: 1px solid var(--border); }
.clinical-order-section h4 { display: flex; align-items: center; gap: 7px; margin: 0 0 10px; font-size: 14px; }
.clinical-order-section h4 span { color: var(--muted); font-weight: 500; }
.medical-record-form { padding: 20px 22px 8px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; }
.span-two { grid-column: 1 / -1; }
.form-grid :deep(.el-select) { width: 100%; }
.dialog-form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 16px; }
.dialog-form-grid :deep(.el-input-number), .outpatient-page :deep(.el-dialog .el-select) { width: 100%; }
@media (max-width: 900px) {
  .outpatient-layout { grid-template-columns: 1fr; }
  .patient-panel { border-right: 0; border-bottom: 1px solid var(--border); }
  .patient-list { min-height: 0; max-height: 290px; overflow-y: auto; }
  .consultation-panel > .el-empty { min-height: 360px; }
  .clinical-order-grid { grid-template-columns: 1fr; }
  .clinical-order-section + .clinical-order-section { border-left: 0; border-top: 1px solid var(--border); }
}
@media (max-width: 640px) {
  .patient-summary { align-items: flex-start; flex-direction: column; }
  .record-actions { width: 100%; justify-content: stretch; }
  .record-actions :deep(.el-button) { flex: 1; margin-left: 0; }
  .form-grid { grid-template-columns: 1fr; }
  .dialog-form-grid { grid-template-columns: 1fr; }
  .span-two { grid-column: auto; }
  .medical-record-form { padding: 16px 14px 4px; }
}
</style>
