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
