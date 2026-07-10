<template>
  <div class="user-profile-page" v-loading="loading">
    <div v-if="user">
      <!-- 封面 -->
      <div class="profile-cover"><div class="cover-gradient"></div></div>

      <!-- 用户信息 -->
      <div class="profile-header">
        <el-avatar :size="96" :src="user.avatar" class="profile-avatar" />
        <div class="profile-info">
          <div class="name-row">
            <h1>{{ user.nickname || user.username }}</h1>
            <span v-if="user.nickname" class="username-tag">@{{ user.username }}</span>
          </div>
          <p class="bio">{{ user.bio || '这个人很懒，什么都没写...' }}</p>
          <div class="meta-row">
            <span class="meta-item"><el-icon><Calendar /></el-icon> {{ formatDate(user.createdAt) }} 加入</span>
            <span class="meta-item"><el-icon><Document /></el-icon> {{ user.postCount || 0 }} 帖子</span>
            <span class="meta-item"><el-icon><Star /></el-icon> {{ user.likeCount || 0 }} 获赞</span>
            <span v-if="user.role === 'ADMIN'" class="admin-badge">管理员</span>
          </div>
          <div v-if="user.location" class="extra-info">
            <span v-if="user.location"><el-icon><Location /></el-icon> {{ user.location }}</span>
            <a v-if="user.website" :href="user.website" target="_blank"><el-icon><Link /></el-icon> {{ user.website }}</a>
            <a v-if="user.github" :href="'https://github.com/' + user.github" target="_blank">🔗 GitHub</a>
          </div>
        </div>
        <div class="header-actions" v-if="!user.isSelf">
          <el-button v-if="user.isFriend" type="success" plain round disabled>
            <el-icon><Check /></el-icon> 已添加好友
          </el-button>
          <el-button v-else-if="requestSent" type="warning" plain round disabled>
            <el-icon><Clock /></el-icon> 等待通过
          </el-button>
          <el-button v-else type="primary" round @click="sendRequest">
            <el-icon><Plus /></el-icon> 添加好友
          </el-button>
          <el-button type="primary" plain round @click="startChat">
            <el-icon><ChatDotRound /></el-icon> 发消息
          </el-button>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="user-posts">
        <h3><el-icon><Document /></el-icon> {{ user.username }} 的帖子</h3>
        <div v-if="posts.length > 0" class="post-list">
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
      </div>
    </div>
    <el-empty v-else description="用户不存在" :image-size="100" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserProfile } from '@/api/auth'
import { getPostList } from '@/api/post'
import { sendFriendRequest, checkFriend } from '@/api/friend'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const user = ref(null)
const posts = ref([])
const loading = ref(true)
const requestSent = ref(false)

onMounted(async () => {
  try {
    const res = await getUserProfile(route.params.id)
    user.value = res.data
    await loadPosts()
  } catch {
    user.value = null
  }
  finally { loading.value = false }
})

async function loadPosts() {
  try {
    const res = await getPostList({ userId: route.params.id })
    posts.value = res.data?.records || []
  } catch { /* ignore */ }
}

async function sendRequest() {
  if (!authStore.isLoggedIn) { router.push('/login'); return }
  try {
    await sendFriendRequest(user.value.id)
    requestSent.value = true
    ElMessage.success('好友请求已发送')
  } catch { /* handled */ }
}

function startChat() {
  if (!authStore.isLoggedIn) { router.push('/login'); return }
  router.push('/chat')
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
.user-profile-page { max-width: 800px; margin: 0 auto; }

.profile-cover {
  height: 160px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 50%, #1a5fa8 100%);
  border-radius: 12px 12px 0 0;
  overflow: hidden;
}
.cover-gradient {
  height: 100%;
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
.name-row h1 { font-size: 22px; font-weight: 700; margin: 0; }
.username-tag { font-size: 14px; color: #909399; }
.bio { color: #606266; font-size: 14px; margin-bottom: 10px; line-height: 1.6; }
.meta-row { display: flex; align-items: center; gap: 18px; flex-wrap: wrap; margin-bottom: 8px; }
.meta-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #909399; }
.admin-badge { background: #e6a23c; color: #fff; font-size: 11px; padding: 1px 8px; border-radius: 10px; }
.extra-info { display: flex; gap: 16px; font-size: 13px; color: #606266; }
.extra-info a { color: #409eff; text-decoration: none; }
.extra-info a:hover { text-decoration: underline; }
.header-actions { display: flex; gap: 10px; align-items: flex-end; padding-bottom: 4px; }

.user-posts {
  margin-top: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  border: 1px solid #ebeef5;
}
.user-posts h3 { font-size: 16px; margin: 0 0 16px; display: flex; align-items: center; gap: 8px; }

.post-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f2f3f5;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.15s;
}
.post-item:hover { background: #f5f7fa; }
.post-item h4 { font-size: 15px; margin: 0 0 6px; }
.post-meta { display: flex; gap: 14px; font-size: 12px; color: #909399; }

@media (max-width: 768px) {
  .profile-header { padding: 0 16px 20px; flex-direction: column; align-items: center; text-align: center; }
  .profile-avatar { margin-top: -40px; width: 80px; height: 80px; }
  .header-actions { padding-top: 8px; }
  .meta-row { justify-content: center; }
  .extra-info { justify-content: center; }
  .name-row { justify-content: center; }
}
</style>
