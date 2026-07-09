<template>
  <div class="profile">
    <div class="profile-card">
      <el-avatar :size="80" :src="user?.avatar" />
      <div class="user-info">
        <h2>{{ user?.username }}</h2>
        <p class="role">{{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</p>
        <p class="bio">{{ user?.bio || '这个人很懒，什么都没写...' }}</p>
        <div class="stats">
          <span>帖子 {{ user?.postCount || 0 }}</span>
          <span>获赞 {{ user?.likeCount || 0 }}</span>
          <span>注册于 {{ user?.createdAt ? dayjs(user.createdAt).format('YYYY-MM-DD') : '-' }}</span>
        </div>
      </div>
    </div>

    <div class="edit-section">
      <h3>编辑资料</h3>
      <el-form :model="form" label-position="top">
        <el-form-item label="个性签名">
          <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="200" show-word-limit
            placeholder="介绍一下自己..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save" :loading="saving">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProfile } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const authStore = useAuthStore()
const user = ref(authStore.user)
const saving = ref(false)

const form = ref({
  bio: user.value?.bio || '',
})

async function save() {
  saving.value = true
  try {
    await updateProfile({ bio: form.value.bio })
    await authStore.fetchUser()
    user.value = authStore.user
    ElMessage.success('保存成功')
  } catch { /* handled */ }
  finally { saving.value = false }
}
</script>

<style scoped>
.profile { max-width: 600px; margin: 0 auto; }

.profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  display: flex;
  gap: 24px;
  align-items: center;
  margin-bottom: 20px;
}

.user-info h2 { font-size: 22px; margin-bottom: 6px; }
.role { font-size: 13px; color: #409eff; margin-bottom: 8px; }
.bio { color: #606266; font-size: 14px; margin-bottom: 12px; }
.stats { display: flex; gap: 20px; font-size: 13px; color: #909399; }

.edit-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.edit-section h3 {
  font-size: 16px;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}
</style>
