<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>登录 TechTalk</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large"
            show-password @keyup.enter="submit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="submit" :loading="loading" style="width:100%">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <p class="switch-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  color: #303133;
}

.switch-link {
  text-align: center;
  margin-top: 16px;
  color: #909399;
  font-size: 14px;
}
</style>
