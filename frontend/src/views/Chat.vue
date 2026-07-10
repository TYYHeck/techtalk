<template>
  <div class="chat-page">
    <!-- 会话列表 -->
    <div class="chat-sidebar" :class="{ hidden: activeConv }">
      <div class="chat-sidebar-header">
        <h3>私信</h3>
        <el-badge v-if="totalUnread > 0" :value="totalUnread" />
      </div>
      <div class="conversation-list" v-if="conversations.length > 0">
        <div
          v-for="conv in conversations" :key="conv.conversationId"
          class="conv-item"
          :class="{ active: activeConv?.conversationId === conv.conversationId }"
          @click="openConversation(conv)"
        >
          <el-avatar :size="44" :src="conv.otherUser.avatar" />
          <div class="conv-info">
            <div class="conv-top">
              <span class="conv-name">{{ conv.otherUser.nickname || conv.otherUser.username }}</span>
              <span class="conv-time">{{ formatTime(conv.lastMessage?.createdAt) }}</span>
            </div>
            <div class="conv-bottom">
              <span class="conv-preview">{{ conv.lastMessage?.content?.slice(0, 30) || '' }}</span>
              <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" class="conv-badge" />
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无会话" :image-size="60" />

      <!-- 好友列表 -->
      <div class="friend-section" v-if="friends.length > 0">
        <h4>好友列表</h4>
        <div v-for="f in friends" :key="f.id" class="friend-item" @click="startChatWith(f)">
          <el-avatar :size="36" :src="f.avatar" />
          <span class="friend-name">{{ f.nickname || f.username }}</span>
        </div>
      </div>
    </div>

    <!-- 聊天窗口 -->
    <div class="chat-window" v-if="activeConv">
      <div class="chat-header">
        <el-button text @click="activeConv = null" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <el-avatar :size="36" :src="activeConv.otherUser.avatar" />
        <span class="chat-title" @click="$router.push(`/user/${activeConv.otherUser.id}`)" style="cursor:pointer">
          {{ activeConv.otherUser.nickname || activeConv.otherUser.username }}
        </span>
        <router-link :to="`/user/${activeConv.otherUser.id}`" class="profile-link">
          <el-button text size="small">查看主页</el-button>
        </router-link>
      </div>
      <div class="chat-messages" ref="msgContainer">
        <div v-for="msg in messages" :key="msg.id" class="msg-item" :class="{ mine: msg.senderId === authStore.user?.id }">
          <el-avatar v-if="msg.senderId !== authStore.user?.id" :size="32" :src="activeConv.otherUser.avatar" />
          <div class="msg-bubble">
            {{ msg.content }}
          </div>
          <el-avatar v-if="msg.senderId === authStore.user?.id" :size="32" :src="authStore.user?.avatar" />
        </div>
        <div ref="msgEnd"></div>
      </div>
      <div class="chat-input">
        <el-input
          v-model="inputMsg"
          type="textarea"
          :rows="3"
          placeholder="输入消息... (Enter 发送，Shift+Enter 换行)"
          maxlength="2000"
          resize="none"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button type="primary" @click="sendMessage" :loading="sending" round>发送</el-button>
      </div>
    </div>

    <!-- 无选中会话 -->
    <div class="chat-empty" v-else>
      <el-empty description="选择一个会话开始聊天" :image-size="100" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getConversations, getMessages, getUnreadCount, markAsRead } from '@/api/chat'
import { getFriends } from '@/api/friend'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const router = useRouter()
const authStore = useAuthStore()

const conversations = ref([])
const friends = ref([])
const activeConv = ref(null)
const messages = ref([])
const inputMsg = ref('')
const sending = ref(false)
const totalUnread = ref(0)
const msgContainer = ref(null)
const msgEnd = ref(null)

let ws = null
let wsTimer = null
let pollTimer = null

onMounted(async () => {
  await Promise.all([loadConversations(), loadFriends(), loadUnread()])
  connectWebSocket()
  pollTimer = setInterval(loadUnread, 15000)
})

onUnmounted(() => {
  if (ws) ws.close()
  if (wsTimer) clearInterval(wsTimer)
  if (pollTimer) clearInterval(pollTimer)
})

function connectWebSocket() {
  const token = authStore.token
  if (!token) return
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  ws = new WebSocket(`${protocol}//${location.host}/ws/chat?token=${token}`)

  ws.onopen = () => { console.log('Chat WebSocket connected') }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'message') {
        // 新消息
        if (activeConv.value && activeConv.value.otherUser.id === data.senderId) {
          messages.value.push(data)
          scrollToBottom()
          markAsRead(activeConv.value.otherUser.id)
        }
        loadConversations()
        loadUnread()
      } else if (data.type === 'ack') {
        // 发送回执，更新消息的 id 和时间
        const lastMsg = messages.value[messages.value.length - 1]
        if (lastMsg && !lastMsg.id) {
          lastMsg.id = data.id
          lastMsg.createdAt = data.createdAt
        }
      }
    } catch { /* ignore */ }
  }

  ws.onclose = () => {
    wsTimer = setTimeout(connectWebSocket, 5000)
  }
}

async function loadConversations() {
  try {
    const res = await getConversations()
    conversations.value = res.data || []
  } catch { /* ignore */ }
}

async function loadFriends() {
  try {
    const res = await getFriends()
    friends.value = res.data || []
  } catch { /* ignore */ }
}

async function loadUnread() {
  try {
    const res = await getUnreadCount()
    totalUnread.value = res.data || 0
  } catch { /* ignore */ }
}

async function openConversation(conv) {
  activeConv.value = conv
  try {
    const res = await getMessages(conv.otherUser.id)
    messages.value = res.data || []
    await nextTick(() => scrollToBottom())
  } catch { /* ignore */ }
}

function startChatWith(friend) {
  // 创建或查找会话
  const existing = conversations.value.find(c => c.otherUser.id === friend.id)
  if (existing) {
    openConversation(existing)
    return
  }
  // 创建临时会话
  const tempConv = {
    conversationId: '',
    otherUser: friend,
    lastMessage: null,
    unreadCount: 0,
  }
  conversations.value.unshift(tempConv)
  activeConv.value = tempConv
  messages.value = []
}

async function sendMessage() {
  const content = inputMsg.value.trim()
  if (!content || !activeConv.value || !activeConv.value.otherUser) return
  sending.value = true

  // 通过 WebSocket 发送
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      receiverId: activeConv.value.otherUser.id,
      content,
    }))
    // 乐观添加到消息列表
    messages.value.push({
      id: null,
      senderId: authStore.user.id,
      receiverId: activeConv.value.otherUser.id,
      content,
      isRead: false,
      createdAt: new Date().toISOString(),
    })
    inputMsg.value = ''
    await nextTick(() => scrollToBottom())
  } else {
    ElMessage.warning('连接已断开，正在重连...')
  }

  sending.value = false
}

function scrollToBottom() {
  if (msgEnd.value) {
    msgEnd.value.scrollIntoView({ behavior: 'smooth' })
  }
}

function formatTime(time) {
  if (!time) return ''
  const d = dayjs(time)
  if (!d.isValid()) return ''
  const now = dayjs()
  const diff = now.diff(d, 'minute')
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff} 分钟前`
  if (d.isSame(now, 'day')) return d.format('HH:mm')
  if (d.isSame(now, 'year')) return d.format('MM-DD')
  return d.format('YYYY-MM-DD')
}
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 56px - 40px);
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  max-width: 900px;
  margin: 0 auto;
}

/* 会话列表 */
.chat-sidebar {
  width: 300px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.chat-sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 8px;
}
.chat-sidebar-header h3 { margin: 0; font-size: 17px; }

.conversation-list { flex: 1; overflow-y: auto; }
.conv-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 20px; cursor: pointer; transition: background 0.15s;
}
.conv-item:hover, .conv-item.active { background: #f0f2f5; }
.conv-info { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.conv-name { font-size: 14px; font-weight: 600; color: #303133; }
.conv-time { font-size: 11px; color: #c0c4cc; }
.conv-bottom { display: flex; justify-content: space-between; align-items: center; }
.conv-preview { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 150px; }
.conv-badge { flex-shrink: 0; }

.friend-section { padding: 16px 20px; border-top: 1px solid #ebeef5; }
.friend-section h4 { font-size: 13px; color: #909399; margin: 0 0 10px; }
.friend-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 0; cursor: pointer; border-radius: 6px;
}
.friend-item:hover { background: #f5f7fa; }
.friend-name { font-size: 14px; color: #303133; }

/* 聊天窗口 */
.chat-window { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.back-btn { display: none; }
.chat-title { font-size: 15px; font-weight: 600; color: #303133; }
.chat-title:hover { color: #409eff; }
.profile-link { margin-left: auto; }

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #f5f6f7;
}
.msg-item {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 16px;
}
.msg-item.mine { flex-direction: row-reverse; }
.msg-bubble {
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
}
.msg-item.mine .msg-bubble { background: #409eff; color: #fff; }

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
  align-items: flex-end;
  flex-shrink: 0;
}
.chat-input .el-textarea { flex: 1; }
.chat-input .el-button { flex-shrink: 0; margin-bottom: 2px; }

.chat-empty { flex: 1; display: flex; align-items: center; justify-content: center; }

@media (max-width: 768px) {
  .chat-page { height: calc(100vh - 56px); border-radius: 0; border: none; }
  .chat-sidebar { width: 100%; }
  .chat-sidebar.hidden { display: none; }
  .chat-window { width: 100%; }
  .back-btn { display: flex; }
  .msg-bubble { max-width: 80%; }
}
</style>
