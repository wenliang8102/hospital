import { http, type ApiResponse } from '@/core/http'

export interface PageResult<T> {
  items: T[]
  page: number
  size: number
  total: number
}

export interface DepartmentOption {
  id: number
  code: string
  name: string
  type: string
}

export interface EmployeeOption {
  id: number
  name: string
  departmentId: number
  registrationLevelId: number
  registrationLevelName: string
  registrationFee: number
}

export interface RegistrationLevelOption {
  id: number
  code: string
  name: string
  fee: number
  quota: number
}

export interface SettlementCategoryOption {
  id: number
  code: string
  name: string
}

export interface Registration {
  id: number
  requestId: string
  caseNumber: string
  realName: string
  gender: 'MALE' | 'FEMALE' | 'UNKNOWN'
  cardNumber: string | null
  birthday: string | null
  age: number | null
  ageType: 'YEAR' | 'DAY' | null
  homeAddress: string | null
  visitDate: string
  noon: 'AM' | 'PM'
  departmentId: number
  departmentName: string
  employeeId: number
  employeeName: string
  registrationLevelId: number
  registrationLevelName: string
  settlementCategoryId: number
  settlementCategoryName: string
  booked: boolean
  registrationMethod: string
  state: 'REGISTERED' | 'IN_CONSULTATION' | 'COMPLETED' | 'CANCELLED'
  registrationFee: number
  createdAt: string
}

export interface CreateRegistrationRequest {
  requestId: string
  caseNumber: string
  realName: string
  gender: string
  cardNumber?: string
  birthday?: string
  age?: number
  ageType?: string
  homeAddress?: string
  visitDate: string
  noon: string
  departmentId: number
  employeeId: number
  registrationLevelId: number
  settlementCategoryId: number
  booked: boolean
  registrationMethod: string
}

export type ChargeItemState = 'UNPAID' | 'PAID' | 'REFUNDED' | 'VOID'

export interface ChargeItem {
  id: number
  registrationId: number
  itemType: string
  sourceId: number
  itemName: string
  unitPrice: number
  quantity: number
  totalAmount: number
  state: ChargeItemState
  paidAt: string | null
  createdAt: string
  originalTransactionId: number | null
}

export interface Payment {
  id: number
  transactionNo: string
  registrationId: number
  transactionType: 'PAYMENT' | 'REFUND'
  paymentMethod: string
  amount: number
  status: 'SUCCESS' | 'FAILED'
  originalTransactionId: number | null
  reason: string | null
  createdAt: string
}

export async function fetchRegistrationOptions() {
  const [departments, levels, settlementCategories] = await Promise.all([
    http.get<ApiResponse<DepartmentOption[]>>('/master-data/departments'),
    http.get<ApiResponse<RegistrationLevelOption[]>>('/master-data/regist-levels'),
    http.get<ApiResponse<SettlementCategoryOption[]>>('/master-data/settle-categories'),
  ])
  return {
    departments: departments.data.data,
    levels: levels.data.data,
    settlementCategories: settlementCategories.data.data,
  }
}

export async function fetchEmployees(departmentId: number) {
  const response = await http.get<ApiResponse<EmployeeOption[]>>('/master-data/employees', {
    params: { departmentId },
  })
  return response.data.data
}

export async function generateCaseNumber() {
  const response = await http.post<ApiResponse<string>>('/registration/case-numbers')
  return response.data.data
}

export async function searchRegistrations(params: Record<string, string | number | undefined>) {
  const response = await http.get<ApiResponse<PageResult<Registration>>>('/registrations', { params })
  return response.data.data
}

export async function createRegistration(request: CreateRegistrationRequest) {
  const response = await http.post<ApiResponse<Registration>>('/registrations', request)
  return response.data.data
}

export async function cancelRegistration(id: number) {
  await http.post(`/registrations/${id}/cancel`)
}

export async function getChargeItems(registrationId: number) {
  const response = await http.get<ApiResponse<ChargeItem[]>>(`/registrations/${registrationId}/charge-items`)
  return response.data.data
}

export async function payChargeItems(
  registrationId: number, chargeItemIds: number[], paymentMethod: string,
) {
  const response = await http.post<ApiResponse<Payment>>('/payments', {
    registrationId,
    chargeItemIds,
    paymentMethod,
    idempotencyKey: crypto.randomUUID(),
  })
  return response.data.data
}

export async function refundChargeItems(
  originalTransactionId: number, chargeItemIds: number[], reason: string,
) {
  const response = await http.post<ApiResponse<Payment>>('/refunds', {
    originalTransactionId,
    chargeItemIds,
    reason,
    idempotencyKey: crypto.randomUUID(),
  })
  return response.data.data
}
