<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Calendar,
  DataBoard,
  Files,
  FirstAidKit,
  Menu as MenuIcon,
  OfficeBuilding,
  SwitchButton,
  Setting,
  User,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { http } from '@/core/http'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const mobileMenuOpen = ref(false)

const navigation = [
  { path: '/', label: '工作台', icon: DataBoard },
  { path: '/platform/users', label: '平台账号', icon: User, permission: 'platform:manage' },
  { path: '/platform/logs', label: '操作日志', icon: Files, permission: 'platform:manage' },
  { path: '/registration', label: '挂号收费', icon: Calendar },
  { path: '/outpatient', label: '门诊诊疗', icon: Files },
  { path: '/medical-tech', label: '医技执行', icon: FirstAidKit },
  { path: '/pharmacy', label: '药房管理', icon: OfficeBuilding },
  { path: '/master-data', label: '基础数据', icon: Setting, permission: 'master-data:read' },
]

const pageTitle = computed(() => String(route.meta.title ?? '工作台'))
const activePath = computed(() => route.path)
const visibleNavigation = computed(() => navigation.filter((item) => !item.permission || auth.hasPermission(item.permission)))

function navigate(path: string) {
  mobileMenuOpen.value = false
  void router.push(path)
}

async function logout() {
  try {
    await http.post('/auth/logout')
  } finally {
    auth.logout()
    await router.replace('/login')
  }
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar desktop-sidebar">
      <div class="brand">
        <span class="brand-mark">H</span>
        <div class="brand-copy">
          <strong>医院信息系统</strong>
          <span>Hospital HIS</span>
        </div>
      </div>

      <el-menu class="navigation" :default-active="activePath" router>
        <el-menu-item v-for="item in visibleNavigation" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <span class="environment-dot" />
        <span>开发环境</span>
      </div>
    </aside>

    <el-drawer v-model="mobileMenuOpen" direction="ltr" size="272px" :with-header="false">
      <div class="mobile-brand brand">
        <span class="brand-mark">H</span>
        <div class="brand-copy">
          <strong>医院信息系统</strong>
          <span>Hospital HIS</span>
        </div>
      </div>
      <el-menu class="navigation mobile-navigation" :default-active="activePath">
        <el-menu-item v-for="item in visibleNavigation" :key="item.path" :index="item.path" @click="navigate(item.path)">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <div class="workspace">
      <header class="topbar">
        <div class="topbar-title">
          <el-tooltip content="打开导航" placement="bottom">
            <button class="icon-button mobile-menu-button" type="button" aria-label="打开导航" @click="mobileMenuOpen = true">
              <el-icon><MenuIcon /></el-icon>
            </button>
          </el-tooltip>
          <div>
            <span class="topbar-eyebrow">门诊业务中心</span>
            <h1>{{ pageTitle }}</h1>
          </div>
        </div>
        <div class="operator">
          <span class="operator-avatar">{{ auth.user?.displayName.slice(0, 1) }}</span>
          <div class="operator-copy">
            <strong>{{ auth.user?.displayName }}</strong>
            <span>{{ auth.user?.roles.join(' / ') }}</span>
          </div>
          <el-tooltip content="退出登录" placement="bottom">
            <button class="icon-button logout-button" type="button" aria-label="退出登录" @click="logout">
              <el-icon><SwitchButton /></el-icon>
            </button>
          </el-tooltip>
        </div>
      </header>

      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>
