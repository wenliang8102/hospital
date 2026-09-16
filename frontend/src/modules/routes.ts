import type { RouteRecordRaw } from 'vue-router'
import { dashboardRoutes } from './dashboard/routes'
import { masterDataRoutes } from './master-data/routes'
import { registrationRoutes } from './registration/routes'
import { outpatientRoutes } from './outpatient/routes'
import { medicalTechRoutes } from './medical-tech/routes'
import { pharmacyRoutes } from './pharmacy/routes'

export const moduleRoutes: RouteRecordRaw[] = [
  ...dashboardRoutes,
  ...registrationRoutes,
  ...outpatientRoutes,
  ...medicalTechRoutes,
  ...pharmacyRoutes,
  ...masterDataRoutes,
]

