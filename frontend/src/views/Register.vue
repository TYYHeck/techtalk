<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <span class="brand-icon">💬</span>
        <span class="brand-text">TechTalk</span>
      </div>
      <h2>创建账号</h2>
      <p class="subtitle">加入 TechTalk，开启技术之旅</p>

      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3-20位）" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item prop="emailCode">
          <div class="code-row">
            <el-input v-model="form.emailCode" placeholder="邮箱验证码" :prefix-icon="Key" size="large" class="code-input" />
            <el-button size="large" :disabled="codeCountdown > 0" :loading="sendingCode" @click.prevent="sendCode" class="code-btn" round native-type="button">
              {{ codeCountdown > 0 ? codeCountdown + 's' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-30位）" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="submit" :loading="loading" style="width:100%" round>注 册</el-button>
        </el-form-item>
      </el-form>

      <p class="switch-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendEmailCode } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Message, Key, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const form = ref({ username: '', email: '', password: '', confirmPassword: '', emailCode: '' })

const validateConfirmPassword = (_rule, value, callback) => {
  if (value === '') { callback(new Error('请再次输入密码')) }
  else if (value !== form.value.password) { callback(new Error('两次输入的密码不一致')) }
  else { callback() }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名只能包含字母、数字、下划线和中文', trigger: 'blur' },
  ],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  emailCode: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }, { len: 6, message: '验证码为 6 位数字', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 30, message: '密码长度 6-30 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }, { validator: validateConfirmPassword, trigger: 'blur' }],
}

async function sendCode() {
  try { await formRef.value.validateField('email') } catch { return }
  sendingCode.value = true
  try {
    await sendEmailCode({ email: form.value.email, purpose: 'register' })
    ElMessage.success('验证码已发送，请查收邮件')
    codeCountdown.value = 60
    countdownTimer = setInterval(() => { codeCountdown.value--; if (codeCountdown.value <= 0) clearInterval(countdownTimer) }, 1000)
  } catch { /* handled */ }
  finally { sendingCode.value = false }
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await register(form.value)
    const { accessToken, refreshToken: refresh, user: userInfo } = res.data
    authStore.token = accessToken; authStore.refreshTokenValue = refresh; authStore.user = userInfo
    localStorage.setItem('token', accessToken); localStorage.setItem('refreshToken', refresh); localStorage.setItem('user', JSON.stringify(userInfo))
    ElMessage.success('注册成功！')
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
  padding: 20px;
}

.auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 36px 36px 32px;
  width: 420px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
  max-height: 90vh;
  overflow-y: auto;
}

.auth-brand { text-align: center; margin-bottom: 16px; display: flex; align-items: center; justify-content: center; gap: 8px; }
.brand-icon { font-size: 26px; }
.brand-text { font-size: 22px; font-weight: 800; color: #409eff; }

h2 { text-align: center; margin-bottom: 4px; font-size: 20px; color: #303133; font-weight: 700; }
.subtitle { text-align: center; color: #909399; font-size: 14px; margin-bottom: 22px; }

.code-row { display: flex; gap: 10px; width: 100%; }
.code-input { flex: 1; }
.code-btn { min-width: 120px; }

.switch-link { text-align: center; margin-top: 16px; color: #909399; font-size: 14px; }

@media (max-width: 480px) {
  .auth-card { width: 100%; padding: 28px 22px; }
  .code-row { flex-direction: column; }
  .code-btn { width: 100%; }
}
</style>
