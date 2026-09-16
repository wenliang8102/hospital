<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { CircleCheck, Clock, Connection, FirstAidKit } from '@element-plus/icons-vue'
import { http, type ApiResponse } from '@/core/http'

interface ModuleInfo {
  code: string
  name: string
  status: string
}

const moduleEndpoints = [
  { name: '挂号收费', endpoint: '/registration/status' },
  { name: '门诊诊疗', endpoint: '/outpatient/status' },
  { name: '医技执行', endpoint: '/medical-tech/status' },
  { name: '药房管理', endpoint: '/pharmacy/status' },
]

const modules = ref(moduleEndpoints.map((item) => ({ ...item, online: null as boolean | null })))

const stats = [
  { label: '今日挂号', value: 0, unit: '人次', icon: FirstAidKit, tone: 'teal' },
  { label: '候诊患者', value: 0, unit: '人', icon: Clock, tone: 'amber' },
  { label: '已完成接诊', value: 0, unit: '人', icon: CircleCheck, tone: 'green' },
  { label: '待执行医技', value: 0, unit: '项', icon: Connection, tone: 'blue' },
]

const workflow = [
  { stage: '挂号', owner: '挂号收费', state: '待接入业务数据' },
  { stage: '接诊', owner: '门诊医生', state: '待接入业务数据' },
  { stage: '医技', owner: '检查 / 检验 / 处置', state: '待接入业务数据' },
  { stage: '发药', owner: '药房', state: '待接入业务数据' },
]

onMounted(async () => {
  await Promise.all(
    modules.value.map(async (item) => {
      try {
        await http.get<ApiResponse<ModuleInfo>>(item.endpoint)
        item.online = true
      } catch {
        item.online = false
      }
    }),
  )
})
</script>

<template>
  <section class="dashboard">
    <div class="section-heading dashboard-heading">
      <div>
        <p class="section-kicker">实时概览</p>
        <h2>今日门诊</h2>
      </div>
      <span class="business-date">{{ new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }).format(new Date()) }}</span>
    </div>

    <div class="stat-grid">
      <article v-for="item in stats" :key="item.label" class="stat-card">
        <span class="stat-icon" :class="`tone-${item.tone}`"><el-icon><component :is="item.icon" /></el-icon></span>
        <div>
          <span class="stat-label">{{ item.label }}</span>
          <p><strong>{{ item.value }}</strong><span>{{ item.unit }}</span></p>
        </div>
      </article>
    </div>

    <div class="dashboard-grid">
      <section class="panel workflow-panel">
        <div class="panel-heading">
          <div>
            <p class="section-kicker">核心流程</p>
            <h3>业务流转</h3>
          </div>
        </div>
        <el-table :data="workflow" table-layout="fixed">
          <el-table-column prop="stage" label="环节" min-width="100" />
          <el-table-column prop="owner" label="责任岗位" min-width="160" />
          <el-table-column prop="state" label="当前状态" min-width="180">
            <template #default="scope"><el-tag type="info" effect="plain">{{ scope.row.state }}</el-tag></template>
          </el-table-column>
        </el-table>
        <ul class="workflow-mobile">
          <li v-for="item in workflow" :key="item.stage">
            <div>
              <strong>{{ item.stage }}</strong>
              <span>{{ item.owner }}</span>
            </div>
            <el-tag type="info" effect="plain" size="small">待接入</el-tag>
          </li>
        </ul>
      </section>

      <section class="panel service-panel">
        <div class="panel-heading">
          <div>
            <p class="section-kicker">运行状态</p>
            <h3>模块连接</h3>
          </div>
        </div>
        <ul class="service-list">
          <li v-for="item in modules" :key="item.name">
            <span>{{ item.name }}</span>
            <el-tag :type="item.online ? 'success' : item.online === false ? 'danger' : 'info'" effect="plain" size="small">
              {{ item.online ? '正常' : item.online === false ? '离线' : '检测中' }}
            </el-tag>
          </li>
        </ul>
      </section>
    </div>
  </section>
</template>
