<template>
  <div class="auth-page">
    <!-- Background decoration -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

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
          <el-button type="primary" size="large" @click="submit" :loading="loading" style="width:100%" round>
            登 录
          </el-button>
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
  try {
    await authStore.login(form.value)
    router.push('/')
  } catch { /* handled */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
  position: relative;
  overflow: hidden;
}

/* Animated background shapes */
.bg-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}
.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}
.shape-1 {
  width: 500px; height: 500px;
  background: #409eff;
  top: -150px; right: -100px;
  animation: float 8s ease-in-out infinite;
}
.shape-2 {
  width: 400px; height: 400px;
  background: #6366f1;
  bottom: -100px; left: -80px;
  animation: float 10s ease-in-out infinite reverse;
}
.shape-3 {
  width: 300px; height: 300px;
  background: #a855f7;
  top: 40%; left: 50%;
  animation: float 12s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

.auth-card {
  background: rgba(255,255,255,0.97);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 44px 40px;
  width: 420px;
  box-shadow: 0 25px 80px rgba(0,0,0,0.3);
  position: relative;
  z-index: 1;
}

.auth-brand {
  text-align: center;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.brand-icon { font-size: 32px; }
.brand-text {
  font-size: 26px;
  font-weight: 800;
  background: linear-gradient(135deg, #409eff, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

h2 {
  text-align: center;
  margin-bottom: 8px;
  font-size: 24px;
  color: #1d2129;
  font-weight: 700;
}

.subtitle {
  text-align: center;
  color: #86909c;
  font-size: 14px;
  margin-bottom: 28px;
}

.switch-link {
  text-align: center;
  margin-top: 20px;
  color: #86909c;
  font-size: 14px;
}

@media (max-width: 480px) {
  .auth-card { width: 90%; padding: 32px 24px; }
}
</style>
