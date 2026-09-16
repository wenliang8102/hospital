<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Connection, Plus, Search } from '@element-plus/icons-vue'
import { http, type ApiResponse } from '@/core/http'

interface ModuleInfo {
  code: string
  name: string
  status: string
}

const props = defineProps<{
  moduleCode: string
  title: string
  accent: string
}>()

const online = ref<boolean | null>(null)
const query = ref('')
const statusText = computed(() => {
  if (online.value === null) return '检测中'
  return online.value ? '服务正常' : '服务未连接'
})

onMounted(async () => {
  try {
    await http.get<ApiResponse<ModuleInfo>>(`/${props.moduleCode}/status`)
    online.value = true
  } catch {
    online.value = false
  }
})
</script>

<template>
  <section class="module-page" :style="{ '--module-accent': accent }">
    <div class="section-heading">
      <div>
        <p class="section-kicker">业务模块</p>
        <h2>{{ title }}</h2>
      </div>
      <el-tag :type="online ? 'success' : online === false ? 'danger' : 'info'" effect="plain">
        <el-icon><Connection /></el-icon>
        {{ statusText }}
      </el-tag>
    </div>

    <div class="toolbar">
      <el-input v-model="query" class="search-input" placeholder="搜索患者姓名或编号" clearable>
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" :icon="Plus">新建</el-button>
    </div>

    <div class="data-surface">
      <el-empty description="暂无业务记录" :image-size="92" />
    </div>
  </section>
</template>

