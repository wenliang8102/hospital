import { http, type ApiResponse } from '@/core/http'

export interface PageResult<T> {
  items: T[]
  page: number
  size: number
  total: number
}

export type VisitState = 'REGISTERED' | 'IN_CONSULTATION' | 'COMPLETED'

export interface Patient {
  id: number
  caseNumber: string
  realName: string
  gender: 'MALE' | 'FEMALE' | 'UNKNOWN'
  cardNumber: string | null
  birthday: string | null
  age: number | null
  ageType: 'YEAR' | 'DAY' | null
  visitDate: string
  noon: 'AM' | 'PM'
  departmentId: number
  departmentName: string
  employeeId: number
  employeeName: string
  registrationLevelName: string
  state: VisitState
  createdAt: string
}

export interface Disease {
  id: number
  code: string
  name: string
  icd: string | null
  category?: string | null
}

export interface MedicalRecordPayload {
  chiefComplaint: string
  presentIllness: string
  presentTreatment: string
  pastHistory: string
  allergyHistory: string
  physicalExamination: string
  examinationProposal: string
  precaution: string
  diagnosis: string
  treatmentPlan: string
  diseaseIds: number[]
}

export interface MedicalRecord extends Omit<MedicalRecordPayload, 'diseaseIds'> {
  id: number
  registrationId: number
  diseases: Disease[]
  createdAt: string
  updatedAt: string
}

export type MedicalOrderType = 'CHECK' | 'INSPECTION' | 'DISPOSAL'
export type OrderState = 'CREATED' | 'PAID' | 'ACCEPTED' | 'EXECUTED' | 'RESULT_REPORTED' | 'CANCELLED' | 'REFUNDED'

export interface MedicalTechnology {
  id: number
  code: string
  name: string
  format: string | null
  price: number
  type: MedicalOrderType
  departmentId: number
}

export interface Drug {
  id: number
  code: string
  name: string
  format: string
  unit: string
  dosage: string | null
  type: string | null
  price: number
  manufacturer: string | null
}

export interface MedicalOrder {
  id: number
  registrationId: number
  type: MedicalOrderType
  medicalTechnologyId: number
  itemName: string
  price: number
  requestInfo: string | null
  bodyPosition: string | null
  remark: string | null
  state: OrderState
  createdAt: string
}

export interface Prescription {
  id: number
  registrationId: number
  drugId: number
  drugName: string
  drugFormat: string
  drugUnit: string
  unitPrice: number
  drugUsage: string
  drugNumber: number
  totalAmount: number
  state: OrderState
  createdAt: string
}

export async function searchPatients(params: Record<string, string | number | undefined>) {
  const response = await http.get<ApiResponse<PageResult<Patient>>>('/outpatient/patients', { params })
  return response.data.data
}

export async function acceptPatient(id: number) {
  const response = await http.post<ApiResponse<Patient>>(`/registrations/${id}/accept`)
  return response.data.data
}

export async function getMedicalRecord(id: number) {
  const response = await http.get<ApiResponse<MedicalRecord>>(`/registrations/${id}/medical-record`)
  return response.data.data
}

export async function saveMedicalRecord(id: number, payload: MedicalRecordPayload) {
  const response = await http.put<ApiResponse<MedicalRecord>>(`/registrations/${id}/medical-record`, payload)
  return response.data.data
}

export async function completeConsultation(id: number) {
  const response = await http.post<ApiResponse<Patient>>(`/registrations/${id}/complete`)
  return response.data.data
}

export async function searchDiseases(keyword?: string) {
  const response = await http.get<ApiResponse<Disease[]>>('/master-data/diseases', {
    params: { keyword: keyword || undefined },
  })
  return response.data.data
}

export async function searchMedicalTechnologies(type?: MedicalOrderType) {
  const response = await http.get<ApiResponse<MedicalTechnology[]>>('/master-data/medical-technologies', {
    params: { type },
  })
  return response.data.data
}

export async function searchDrugs(keyword?: string) {
  const response = await http.get<ApiResponse<Drug[]>>('/master-data/drugs', {
    params: { keyword: keyword || undefined },
  })
  return response.data.data
}

export async function getMedicalOrders(registrationId: number) {
  const response = await http.get<ApiResponse<MedicalOrder[]>>(`/registrations/${registrationId}/medical-orders`)
  return response.data.data
}

export async function createMedicalOrder(registrationId: number, request: {
  type: MedicalOrderType
  medicalTechnologyId: number
  requestInfo?: string
  bodyPosition?: string
  remark?: string
}) {
  const response = await http.post<ApiResponse<MedicalOrder[]>>(
    `/registrations/${registrationId}/medical-orders`, [request],
  )
  return response.data.data
}

export async function cancelMedicalOrder(registrationId: number, order: MedicalOrder) {
  await http.post(`/registrations/${registrationId}/medical-orders/${order.type}/${order.id}/cancel`)
}

export async function getPrescriptions(registrationId: number) {
  const response = await http.get<ApiResponse<Prescription[]>>(`/registrations/${registrationId}/prescriptions`)
  return response.data.data
}

export async function createPrescription(registrationId: number, request: {
  drugId: number
  drugUsage: string
  drugNumber: number
}) {
  const response = await http.post<ApiResponse<Prescription[]>>(
    `/registrations/${registrationId}/prescriptions`, [request],
  )
  return response.data.data
}

export async function cancelPrescription(registrationId: number, prescriptionId: number) {
  await http.post(`/registrations/${registrationId}/prescriptions/${prescriptionId}/cancel`)
}
