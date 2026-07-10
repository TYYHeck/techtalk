<template>
  <div class="security-page">
    <div class="page-header">
      <router-link to="/profile" class="back-link">
        <el-icon><ArrowLeft /></el-icon> 返回个人主页
      </router-link>
      <h2>账户安全</h2>
    </div>

    <!-- 修改密码 -->
    <div class="security-card">
      <h3><el-icon><Lock /></el-icon> 修改密码</h3>
      <el-form :model="pwdForm" label-position="top" class="security-form">
        <el-form-item label="当前密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
        <el-button type="primary" @click="updatePwd" :loading="pwdLoading">修改密码</el-button>
      </el-form>
    </div>

    <!-- 修改邮箱 -->
    <div class="security-card">
      <h3><el-icon><Message /></el-icon> 修改邮箱</h3>
      <el-form :model="emailForm" label-position="top" class="security-form">
        <el-form-item label="当前邮箱">
          <el-input :model-value="authStore.user?.email" disabled />
        </el-form-item>
        <el-form-item label="新邮箱">
          <el-input v-model="emailForm.email" placeholder="请输入新邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="emailForm.code" placeholder="邮箱验证码" />
            <el-button :disabled="codeCountdown > 0" @click="sendCode" :loading="codeSending">
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-button type="primary" @click="updateEmail" :loading="emailLoading">修改邮箱</el-button>
      </el-form>
    </div>

    <!-- 编辑个人资料 -->
    <div class="security-card">
      <h3><el-icon><User /></el-icon> 个人资料</h3>
      <el-form :model="profileForm" label-position="top" class="security-form">
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="设置昵称" maxlength="50" />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input v-model="profileForm.bio" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="介绍一下自己..." />
        </el-form-item>
        <el-form-item label="所在地">
          <el-input v-model="profileForm.location" placeholder="例如：北京" maxlength="50" />
        </el-form-item>
        <el-form-item label="个人网站">
          <el-input v-model="profileForm.website" placeholder="https://..." maxlength="100" />
        </el-form-item>
        <el-form-item label="GitHub 用户名">
          <el-input v-model="profileForm.github" placeholder="GitHub 用户名" maxlength="50" />
        </el-form-item>
        <el-button type="primary" @click="saveProfile" :loading="profileLoading">保存资料</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { updateProfile, updatePassword, updateEmail as updateEmailApi, sendEmailCode } from '@/api/auth'

const authStore = useAuthStore()

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false)

const emailForm = reactive({ email: '', code: '' })
const emailLoading = ref(false)
const codeSending = ref(false)
const codeCountdown = ref(0)

const profileForm = reactive({ nickname: '', bio: '', location: '', website: '', github: '' })
const profileLoading = ref(false)

onMounted(() => {
  const u = authStore.user
  if (u) {
    profileForm.nickname = u.nickname || ''
    profileForm.bio = u.bio || ''
    profileForm.location = u.location || ''
    profileForm.website = u.website || ''
    profileForm.github = u.github || ''
  }
})

async function updatePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次新密码不一致')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  pwdLoading.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    authStore.logout()
    window.location.href = '/login'
  } catch { /* handled */ }
  finally { pwdLoading.value = false }
}

async function sendCode() {
  if (!emailForm.email) { ElMessage.warning('请输入新邮箱'); return }
  codeSending.value = true
  try {
    await sendEmailCode({ email: emailForm.email, purpose: 'resetPassword' })
    ElMessage.success('验证码已发送')
    codeCountdown.value = 60
    const timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch { /* handled */ }
  finally { codeSending.value = false }
}

async function updateEmail() {
  if (!emailForm.email || !emailForm.code) { ElMessage.warning('请填写完整'); return }
  emailLoading.value = true
  try {
    await updateEmailApi(emailForm)
    ElMessage.success('邮箱修改成功')
    await authStore.fetchUser()
    emailForm.email = ''
    emailForm.code = ''
  } catch { /* handled */ }
  finally { emailLoading.value = false }
}

async function saveProfile() {
  profileLoading.value = true
  try {
    await updateProfile({ ...profileForm })
    await authStore.fetchUser()
    ElMessage.success('资料保存成功')
  } catch { /* handled */ }
  finally { profileLoading.value = false }
}
</script>

<style scoped>
.security-page { max-width: 640px; margin: 0 auto; }
.page-header { margin-bottom: 20px; }
.back-link {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 13px; color: #909399; text-decoration: none; margin-bottom: 8px;
}
.back-link:hover { color: #409eff; }
.page-header h2 { font-size: 22px; margin: 0; color: #303133; }

.security-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
}
.security-card h3 {
  font-size: 16px; margin: 0 0 18px; padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5; display: flex; align-items: center; gap: 8px;
}
.security-form { max-width: 400px; }
.code-row { display: flex; gap: 12px; }
.code-row .el-input { flex: 1; }
.code-row .el-button { flex-shrink: 0; white-space: nowrap; }

@media (max-width: 768px) {
  .security-page { padding: 0 8px; }
}
</style>
