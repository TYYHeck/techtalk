<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <span class="logo-icon">💬</span>
          <span class="logo-text">TechTalk</span>
        </router-link>
        <nav class="nav">
          <router-link to="/" class="nav-item">首页</router-link>
          <template v-if="authStore.isLoggedIn">
            <router-link to="/create" class="nav-item">
              <el-button type="primary" size="small">发布帖子</el-button>
            </router-link>
            <el-dropdown @command="handleCommand" trigger="click">
              <span class="user-area">
                <el-avatar :size="32" :src="authStore.user?.avatar" />
                <span class="username">{{ authStore.user?.username }}</span>
                <el-badge v-if="notifStore.unreadCount > 0" :value="notifStore.unreadCount" :max="99" class="badge" />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                  <el-dropdown-item command="notifications">
                    消息通知
                    <el-badge v-if="notifStore.unreadCount > 0" :value="notifStore.unreadCount" style="margin-left:8px" />
                  </el-dropdown-item>
                  <el-dropdown-item v-if="authStore.isAdmin" command="admin" divided>管理后台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-item">登录</router-link>
            <router-link to="/register" class="nav-item">
              <el-button type="primary" size="small" plain>注册</el-button>
            </router-link>
          </template>
        </nav>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">
      <p>TechTalk 技术社区 © 2024 | Spring Boot + Vue 3 + MySQL + Redis</p>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const authStore = useAuthStore()
const notifStore = useNotificationStore()

let notifTimer = null

function startNotifPolling() {
  if (authStore.isLoggedIn) {
    notifStore.fetchUnreadCount()
    notifTimer = setInterval(() => {
      // 如果已登出，停止轮询
      if (!authStore.isLoggedIn) {
        clearInterval(notifTimer)
        notifTimer = null
        return
      }
      notifStore.fetchUnreadCount()
    }, 30000)
  }
}

onMounted(() => {
  startNotifPolling()
})

onUnmounted(() => {
  if (notifTimer) {
    clearInterval(notifTimer)
    notifTimer = null
  }
})

function handleCommand(cmd) {
  switch (cmd) {
    case 'profile': router.push('/profile'); break
    case 'favorites': router.push('/favorites'); break
    case 'notifications': router.push('/notifications'); break
    case 'admin': router.push('/admin'); break
    case 'logout':
      authStore.logout()
      router.push('/')
      break
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
  font-size: 22px;
  font-weight: 700;
}

.logo-icon { font-size: 28px; }

.nav {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-item {
  color: #606266;
  font-size: 14px;
  transition: color 0.2s;
}

.nav-item:hover { color: #409eff; }

.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-area:hover { background: #f0f2f5; }

.username {
  font-size: 14px;
  color: #303133;
}

.badge { margin-left: 4px; }

.main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 20px auto;
  padding: 0 20px;
}

.footer {
  text-align: center;
  padding: 24px;
  color: #909399;
  font-size: 13px;
  border-top: 1px solid #ebeef5;
}
</style>
