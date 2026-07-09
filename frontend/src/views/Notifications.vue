<template>
  <div class="notifications">
    <div class="header-bar">
      <h2>消息通知</h2>
      <el-button type="primary" text @click="markAllRead" :disabled="notifStore.unreadCount === 0">
        全部标为已读
      </el-button>
    </div>

    <div v-if="notifications.length === 0" class="empty">
      <el-empty description="暂无通知" />
    </div>

    <div v-else class="notif-list">
      <div v-for="notif in notifications" :key="notif.id"
        class="notif-item" :class="{ unread: !notif.isRead }"
        @click="handleClick(notif)">
        <div class="notif-icon">
          <el-icon v-if="notif.type === 'LIKE'" color="#f56c6c" :size="20"><StarFilled /></el-icon>
          <el-icon v-else-if="notif.type === 'COMMENT'" color="#409eff" :size="20"><ChatDotRound /></el-icon>
          <el-icon v-else-if="notif.type === 'REPLY'" color="#67c23a" :size="20"><ChatLineSquare /></el-icon>
          <el-icon v-else color="#e6a23c" :size="20"><Bell /></el-icon>
        </div>
        <div class="notif-content">
          <h4>{{ notif.title }}</h4>
          <p>{{ notif.content }}</p>
          <span class="notif-time">{{ dayjs(notif.createdAt).fromNow() }}</span>
        </div>
        <div v-if="!notif.isRead" class="unread-dot" />
      </div>
    </div>

    <div class="pagination" v-if="total > size">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
        layout="prev, pager, next" background @current-change="fetchNotifications" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markAllAsRead } from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const notifStore = useNotificationStore()
const notifications = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => fetchNotifications())

async function fetchNotifications() {
  try {
    const res = await getNotifications({ page: page.value, size: size.value })
    notifications.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* */ }
}

async function markAllRead() {
  await markAllAsRead()
  notifStore.unreadCount = 0
  await fetchNotifications()
}

function handleClick(notif) {
  if (notif.postId) {
    router.push(`/post/${notif.postId}`)
  }
}
</script>

<style scoped>
.notifications { max-width: 700px; margin: 0 auto; }

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-bar h2 { font-size: 20px; }

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.notif-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }

.notif-item.unread { background: #ecf5ff; border-left: 3px solid #409eff; }

.notif-icon { padding-top: 2px; }

.notif-content { flex: 1; }

.notif-content h4 { font-size: 15px; color: #303133; margin-bottom: 4px; }
.notif-content p { font-size: 13px; color: #606266; line-height: 1.5; }
.notif-time { font-size: 12px; color: #c0c4cc; margin-top: 4px; display: inline-block; }

.unread-dot {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  position: absolute;
  top: 20px;
  right: 16px;
}

.empty { margin-top: 40px; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
