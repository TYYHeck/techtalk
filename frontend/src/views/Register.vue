<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>注册 TechTalk</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3-20位）" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-30位）" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="submit" :loading="loading" style="width:100%">
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <p class="switch-link">已有账号？<router-link to="/login">立即登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = ref({ username: '', email: '', password: '' })
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名只能包含字母、数字、下划线和中文', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度 6-30 位', trigger: 'blur' },
  ],
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await register(form.value)
    // 注册成功，保存 token 并跳转
    const { accessToken, refreshToken: refresh, user: userInfo } = res.data
    authStore.token = accessToken
    authStore.refreshTokenValue = refresh
    authStore.user = userInfo
    localStorage.setItem('token', accessToken)
    localStorage.setItem('refreshToken', refresh)
    localStorage.setItem('user', JSON.stringify(userInfo))
    ElMessage.success('注册成功！')
    router.push('/')
  } catch { /* handled by interceptor */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}

h2 { text-align: center; margin-bottom: 30px; font-size: 24px; color: #303133; }

.switch-link { text-align: center; margin-top: 16px; color: #909399; font-size: 14px; }
</style>
