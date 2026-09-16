import { expect, test, type Page } from '@playwright/test'

const envelope = (data: unknown) => ({
  code: 'OK',
  message: 'success',
  data,
  timestamp: '2026-09-16T12:00:00Z',
})

async function mockRegistrationApi(page: Page) {
  let chargeState = 'UNPAID'
  await page.addInitScript(() => {
    localStorage.setItem('hospital-his.auth', JSON.stringify({
      token: 'visual-test-token',
      user: {
        id: 1,
        username: 'admin',
        displayName: '系统管理员',
        employeeId: null,
        roles: ['ROOT'],
        permissions: ['registration:write'],
      },
    }))
  })

  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url())
    let data: unknown = []
    if (url.pathname.endsWith('/master-data/departments')) {
      data = [{ id: 1, code: 'INTERNAL', name: '内科', type: 'OUTPATIENT' }]
    } else if (url.pathname.endsWith('/master-data/regist-levels')) {
      data = [{ id: 1, code: 'GENERAL', name: '普通号', fee: 8, quota: 10 }]
    } else if (url.pathname.endsWith('/master-data/settle-categories')) {
      data = [{ id: 1, code: 'SELF_PAY', name: '自费' }]
    } else if (url.pathname.endsWith('/master-data/employees')) {
      data = [{ id: 1, name: '李医生', departmentId: 1, registrationLevelId: 1, registrationLevelName: '普通号', registrationFee: 8 }]
    } else if (url.pathname.endsWith('/registration/case-numbers')) {
      data = 'H20260916A1B2C3D4'
    } else if (url.pathname.includes('/charge-items')) {
      data = [{ id: 1, registrationId: 1, itemType: 'REGISTRATION', sourceId: 1, itemName: '普通号挂号费', unitPrice: 8, quantity: 1, totalAmount: 8, state: chargeState, paidAt: null, createdAt: '2026-09-16T08:00:00', originalTransactionId: null }]
    } else if (url.pathname.endsWith('/payments')) {
      chargeState = 'PAID'
      data = { id: 1, transactionNo: 'payment-001', registrationId: 1, transactionType: 'PAYMENT', paymentMethod: 'CASH', amount: 8, status: 'SUCCESS', originalTransactionId: null, reason: null, createdAt: '2026-09-16T08:01:00' }
    } else if (url.pathname.endsWith('/registrations')) {
      data = { items: [{ id: 1, requestId: 'request-1', caseNumber: 'H20260916A1B2C3D4', realName: '张三', gender: 'MALE', cardNumber: null, birthday: null, age: 36, ageType: 'YEAR', homeAddress: null, visitDate: '2026-09-17T08:00:00', noon: 'AM', departmentId: 1, departmentName: '内科', employeeId: 1, employeeName: '李医生', registrationLevelId: 1, registrationLevelName: '普通号', settlementCategoryId: 1, settlementCategoryName: '自费', booked: false, registrationMethod: 'CASH', state: 'REGISTERED', registrationFee: 8, createdAt: '2026-09-16T08:00:00' }], page: 1, size: 20, total: 1 }
    }
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(envelope(data)) })
  })
}

for (const viewport of [
  { name: 'desktop', width: 1440, height: 900 },
  { name: 'mobile', width: 390, height: 844 },
]) {
  test(`registration workspace renders at ${viewport.name} size`, async ({ page }, testInfo) => {
    await page.setViewportSize(viewport)
    await mockRegistrationApi(page)
    await page.goto('/registration')

    await expect(page.getByRole('heading', { name: '挂号工作台' })).toBeVisible()
    await expect(page.getByText('张三')).toBeVisible()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)

    await page.getByRole('button', { name: '收费', exact: true }).click()
    const billingDrawer = page.getByRole('dialog', { name: '收费结算' })
    await expect(billingDrawer).toBeVisible()
    await page.waitForTimeout(400)
    await page.screenshot({ path: testInfo.outputPath(`billing-${viewport.name}.png`), fullPage: true })
    await billingDrawer.locator('.el-table__body-wrapper .el-checkbox').click()
    await billingDrawer.getByRole('button', { name: '确认收费' }).click()
    await page.locator('.el-message-box__btns .el-button--primary').click()
    await expect(page.getByText('收费成功')).toBeVisible()
    await page.keyboard.press('Escape')

    await page.getByRole('button', { name: '新建挂号' }).click()
    await expect(page.getByRole('dialog', { name: '新建挂号' })).toBeVisible()
    await expect(page.locator('.el-dialog input').first()).toHaveValue('H20260916A1B2C3D4')
    await page.waitForTimeout(400)
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)

    await page.screenshot({ path: testInfo.outputPath(`${viewport.name}.png`), fullPage: true })
    const submit = page.getByRole('button', { name: '确认挂号' })
    await submit.scrollIntoViewIfNeeded()
    await expect(submit).toBeVisible()
  })
}
