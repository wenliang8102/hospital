import { expect, test, type Page } from '@playwright/test'

const envelope = (data: unknown) => ({
  code: 'OK',
  message: 'success',
  data,
  timestamp: '2026-09-16T12:00:00Z',
})

async function mockRegistrationApi(page: Page) {
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
    } else if (url.pathname.endsWith('/registrations')) {
      data = { items: [], page: 1, size: 20, total: 0 }
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
    await expect(page.getByText('暂无挂号记录')).toBeVisible()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)

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
