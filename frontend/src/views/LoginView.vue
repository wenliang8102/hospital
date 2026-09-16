<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, min: 3, max: 64, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 5, max: 128, message: '密码至少 5 位', trigger: 'blur' }],
}
const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

async function submit() {
  if (!await formRef.value?.validate().catch(() => false)) return
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch {
    ElMessage.error('用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel" aria-labelledby="login-title">
      <div class="login-brand">
        <span class="brand-mark">H</span>
        <div>
          <strong>医院信息系统</strong>
          <span>Hospital HIS</span>
        </div>
      </div>
      <div class="login-heading">
        <h1 id="login-title">账户登录</h1>
        <p>进入医院业务工作台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :prefix-icon="User" autocomplete="username" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password autocomplete="current-password" size="large" />
        </el-form-item>
        <el-button class="login-submit" type="primary" native-type="submit" :loading="loading" size="large">登录</el-button>
      </el-form>
    </section>
  </main>
</template>
