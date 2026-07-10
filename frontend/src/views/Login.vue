<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <span class="brand-icon">💬</span>
        <span class="brand-text">TechTalk</span>
      </div>
      <h2>欢迎回来</h2>
      <p class="subtitle">登录你的账号，加入技术讨论</p>

      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="submit" :loading="loading" style="width:100%" round>登 录</el-button>
        </el-form-item>
      </el-form>

      <p class="switch-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)
const form = ref({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try { await authStore.login(form.value); router.push('/') } catch { /* handled */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}

.auth-brand { text-align: center; margin-bottom: 20px; display: flex; align-items: center; justify-content: center; gap: 8px; }
.brand-icon { font-size: 28px; }
.brand-text { font-size: 24px; font-weight: 800; color: #409eff; }

h2 { text-align: center; margin-bottom: 6px; font-size: 22px; color: #303133; font-weight: 700; }
.subtitle { text-align: center; color: #909399; font-size: 14px; margin-bottom: 28px; }
.switch-link { text-align: center; margin-top: 18px; color: #909399; font-size: 14px; }

@media (max-width: 480px) {
  .auth-card { width: 100%; padding: 32px 24px; }
}
</style>
