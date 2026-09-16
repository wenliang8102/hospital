import { expect, test, type Page } from '@playwright/test'

const envelope = (data: unknown) => ({
  code: 'OK',
  message: 'success',
  data,
  timestamp: '2026-09-16T12:00:00Z',
})

const basePatient = {
  id: 11,
  caseNumber: 'H20260916A1B2C3D4',
  realName: '张三',
  gender: 'MALE',
  cardNumber: '110101199001011234',
  birthday: '1990-01-01',
  age: 36,
  ageType: 'YEAR',
  visitDate: '2026-09-16T08:00:00',
  noon: 'AM',
  departmentId: 1,
  departmentName: '内科',
  employeeId: 1,
  employeeName: '李医生',
  registrationLevelName: '普通号',
  createdAt: '2026-09-16T07:50:00',
}

async function mockOutpatientApi(page: Page) {
  let state = 'REGISTERED'
  await page.addInitScript(() => {
    localStorage.setItem('hospital-his.auth', JSON.stringify({
      token: 'doctor-test-token',
      user: {
        id: 2,
        username: 'doctor',
        displayName: '李医生',
        employeeId: 1,
        roles: ['OUTPATIENT_DOCTOR'],
        permissions: ['outpatient:write'],
      },
    }))
  })

  await page.route('**/api/**', async (route) => {
    const request = route.request()
    const url = new URL(request.url())
    let data: unknown = []
    if (url.pathname.endsWith('/outpatient/patients')) {
      data = { items: [{ ...basePatient, state }], page: 1, size: 20, total: 1 }
    } else if (url.pathname.endsWith('/master-data/diseases')) {
      data = [{ id: 1, code: 'J00', name: '急性鼻咽炎', icd: 'J00' }]
    } else if (url.pathname.endsWith('/accept')) {
      state = 'IN_CONSULTATION'
      data = { ...basePatient, state }
    } else if (url.pathname.endsWith('/medical-record') && request.method() === 'PUT') {
      const payload = request.postDataJSON()
      data = {
        id: 1,
        registrationId: 11,
        ...payload,
        diseases: [{ id: 1, code: 'J00', name: '急性鼻咽炎', icd: 'J00' }],
        createdAt: '2026-09-16T12:00:00',
        updatedAt: '2026-09-16T12:00:00',
      }
    }
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(envelope(data)) })
  })
}

for (const viewport of [
  { name: 'desktop', width: 1440, height: 900 },
  { name: 'mobile', width: 390, height: 844 },
]) {
  test(`outpatient consultation works at ${viewport.name} size`, async ({ page }, testInfo) => {
    await page.setViewportSize(viewport)
    await mockOutpatientApi(page)
    await page.goto('/outpatient')

    await expect(page.getByRole('heading', { name: '医生工作台' })).toBeVisible()
    await page.getByRole('button', { name: /张三/ }).click()
    await expect(page.getByRole('button', { name: '接诊' })).toBeVisible()
    await page.getByRole('button', { name: '接诊' }).click()
    await expect(page.getByRole('button', { name: '保存病历' })).toBeVisible()

    await page.getByLabel('主诉').fill('鼻塞、流涕一天')
    await page.locator('.el-form-item').filter({ hasText: '最终诊断' }).locator('.el-select__wrapper').click()
    await page.getByRole('option', { name: 'J00 急性鼻咽炎' }).click()
    await page.getByRole('button', { name: '保存病历' }).click()
    await expect(page.getByText('病历已保存')).toBeVisible()
    await expect(page.getByRole('button', { name: '完成看诊' })).toBeEnabled()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)

    await page.waitForTimeout(3200)
    await page.evaluate(() => window.scrollTo(0, 0))
    await page.screenshot({ path: testInfo.outputPath(`${viewport.name}.png`), fullPage: true })
  })
}
