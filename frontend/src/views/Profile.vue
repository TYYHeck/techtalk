<template>
  <div class="profile-page">
    <!-- 封面区域 -->
    <div class="profile-cover">
      <div class="cover-gradient"></div>
    </div>

    <!-- 用户信息卡片 -->
    <div class="profile-header">
      <el-avatar :size="96" :src="user?.avatar" class="profile-avatar" />
      <div class="profile-info">
        <div class="name-row">
          <h1>{{ user?.nickname || user?.username }}</h1>
          <span v-if="user?.nickname" class="username-tag">@{{ user?.username }}</span>
        </div>
        <p class="bio">{{ user?.bio || '这个人很懒，什么都没写...' }}</p>
        <div class="meta-row">
          <span class="meta-item"><el-icon><Calendar /></el-icon> {{ formatDate(user?.createdAt) }} 加入</span>
          <span class="meta-item"><el-icon><Document /></el-icon> {{ user?.postCount || 0 }} 帖子</span>
          <span class="meta-item"><el-icon><Star /></el-icon> {{ user?.likeCount || 0 }} 获赞</span>
          <span v-if="user?.role === 'ADMIN'" class="admin-badge">管理员</span>
        </div>
        <div v-if="user?.location" class="extra-info">
          <span v-if="user.location"><el-icon><Location /></el-icon> {{ user.location }}</span>
          <a v-if="user.website" :href="user.website" target="_blank"><el-icon><Link /></el-icon> {{ user.website }}</a>
          <a v-if="user.github" :href="'https://github.com/' + user.github" target="_blank">🔗 GitHub</a>
        </div>
      </div>
      <router-link to="/profile/security" class="edit-btn">
        <el-button type="primary" plain round size="default">
          <el-icon><Edit /></el-icon> 编辑资料
        </el-button>
      </router-link>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" class="profile-tabs">
      <el-tab-pane label="帖子" name="posts">
        <div class="post-list" v-if="posts.length > 0">
          <div v-for="post in posts" :key="post.id" class="post-item" @click="$router.push(`/post/${post.id}`)">
            <h4>{{ post.title }}</h4>
            <div class="post-meta">
              <span>{{ post.categoryName }}</span>
              <span>{{ formatTime(post.createdAt) }}</span>
              <span>{{ post.viewCount }} 浏览</span>
              <span>{{ post.commentCount }} 评论</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无帖子" :image-size="80" />
      </el-tab-pane>
      <el-tab-pane label="个人资料" name="about">
        <div class="about-section">
          <div class="about-card">
            <h3>基本信息</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ user?.nickname || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所在地">{{ user?.location || '-' }}</el-descriptions-item>
              <el-descriptions-item label="个人网站">{{ user?.website || '-' }}</el-descriptions-item>
              <el-descriptions-item label="GitHub">{{ user?.github || '-' }}</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ formatDate(user?.createdAt) }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getUserProfile } from '@/api/auth'
import { getPostList } from '@/api/post'
import dayjs from 'dayjs'

const authStore = useAuthStore()
const user = ref(authStore.user)
const activeTab = ref('posts')
const posts = ref([])

onMounted(async () => {
  if (authStore.isLoggedIn) {
    await authStore.fetchUser()
    user.value = authStore.user
    loadPosts()
  }
})

async function loadPosts() {
  try {
    const res = await getPostList({ userId: user.value.id })
    posts.value = res.data?.records || []
  } catch { /* ignore */ }
}

function formatDate(date) {
  return date ? dayjs(date).format('YYYY年MM月DD日') : '-'
}

function formatTime(time) {
  if (!time) return ''
  const d = dayjs(time)
  const now = dayjs()
  const diff = now.diff(d, 'minute')
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff} 分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)} 小时前`
  return d.format('YYYY-MM-DD')
}
</script>

<style scoped>
.profile-page { max-width: 800px; margin: 0 auto; }

.profile-cover {
  height: 160px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 50%, #1a5fa8 100%);
  border-radius: 12px 12px 0 0;
  position: relative;
  overflow: hidden;
}
.cover-gradient {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 50%, rgba(255,255,255,0.1) 0%, transparent 60%);
}

.profile-header {
  background: #fff;
  padding: 0 32px 24px;
  display: flex;
  align-items: flex-end;
  gap: 24px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 12px 12px;
  position: relative;
  flex-wrap: wrap;
}

.profile-avatar {
  margin-top: -48px;
  border: 4px solid #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

.profile-info { flex: 1; min-width: 0; padding-top: 8px; }
.name-row { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.name-row h1 { font-size: 22px; font-weight: 700; margin: 0; color: #303133; }
.username-tag { font-size: 14px; color: #909399; }
.bio { color: #606266; font-size: 14px; margin-bottom: 10px; line-height: 1.6; }
.meta-row { display: flex; align-items: center; gap: 18px; flex-wrap: wrap; margin-bottom: 8px; }
.meta-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #909399; }
.admin-badge { background: #e6a23c; color: #fff; font-size: 11px; padding: 1px 8px; border-radius: 10px; }
.extra-info { display: flex; gap: 16px; font-size: 13px; color: #606266; }
.extra-info a { color: #409eff; text-decoration: none; }
.extra-info a:hover { text-decoration: underline; }

.edit-btn { position: absolute; top: 20px; right: 32px; }

.profile-tabs {
  margin-top: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  border: 1px solid #ebeef5;
}

.post-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f2f3f5;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.15s;
}
.post-item:hover { background: #f5f7fa; }
.post-item h4 { font-size: 15px; margin: 0 0 6px; color: #303133; }
.post-meta { display: flex; gap: 14px; font-size: 12px; color: #909399; }

.about-section { padding: 8px 0; }
.about-card h3 { font-size: 16px; margin-bottom: 14px; color: #303133; }

@media (max-width: 768px) {
  .profile-header { padding: 0 16px 20px; flex-direction: column; align-items: center; text-align: center; }
  .profile-avatar { margin-top: -40px; width: 80px; height: 80px; }
  .edit-btn { position: static; margin-top: 12px; }
  .meta-row { justify-content: center; }
  .extra-info { justify-content: center; }
  .name-row { justify-content: center; }
  .profile-tabs { padding: 16px; }
}
</style>
