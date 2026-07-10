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
              <el-button type="primary" size="default" round>
                <el-icon><EditPen /></el-icon> 发布
              </el-button>
            </router-link>
            <el-dropdown @command="handleCommand" trigger="click" placement="bottom-end">
              <span class="user-area">
                <el-avatar :size="34" :src="authStore.user?.avatar" />
                <span class="username">{{ authStore.user?.username }}</span>
                <el-badge v-if="notifStore.unreadCount > 0" :value="notifStore.unreadCount" :max="99" class="badge" />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon> 个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="favorites">
                    <el-icon><Star /></el-icon> 我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item command="notifications">
                    <el-icon><Bell /></el-icon> 消息通知
                    <el-badge v-if="notifStore.unreadCount > 0" :value="notifStore.unreadCount" style="margin-left:8px" />
                  </el-dropdown-item>
                  <el-dropdown-item v-if="authStore.isAdmin" command="admin" divided>
                    <el-icon><Setting /></el-icon> 管理后台
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-link">登录</router-link>
            <router-link to="/register">
              <el-button type="primary" size="default" round plain>注册</el-button>
            </router-link>
          </template>
        </nav>
      </div>
    </header>
    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <footer class="footer">
      <div class="footer-inner">
        <span>TechTalk 技术社区 &copy; 2024</span>
        <span class="footer-tech">Spring Boot 3 + Vue 3 + MySQL + Redis</span>
      </div>
    </footer>
    <!-- Back to top -->
    <transition name="fade">
      <div v-show="showBackTop" class="back-top" @click="scrollToTop">
        <el-icon :size="20"><ArrowUp /></el-icon>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const authStore = useAuthStore()
const notifStore = useNotificationStore()

let notifTimer = null
const showBackTop = ref(false)

function startNotifPolling() {
  if (authStore.isLoggedIn) {
    notifStore.fetchUnreadCount()
    notifTimer = setInterval(() => {
      if (!authStore.isLoggedIn) {
        clearInterval(notifTimer)
        notifTimer = null
        return
      }
      notifStore.fetchUnreadCount()
    }, 30000)
  }
}

function onScroll() {
  showBackTop.value = window.scrollY > 400
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  startNotifPolling()
  window.addEventListener('scroll', onScroll)
})

onUnmounted(() => {
  if (notifTimer) {
    clearInterval(notifTimer)
    notifTimer = null
  }
  window.removeEventListener('scroll', onScroll)
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
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
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
  padding: 0 24px;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1d2129;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.5px;
}
.logo-icon { font-size: 26px; }
.logo-text {
  background: linear-gradient(135deg, #409eff, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item { color: #4e5969; font-size: 14px; padding: 6px 12px; border-radius: 6px; transition: all 0.2s; }
.nav-item:hover { color: #409eff; background: rgba(64,158,255,0.06); }

.nav-link { color: #4e5969; font-size: 14px; padding: 6px 12px; border-radius: 6px; transition: all 0.2s; }
.nav-link:hover { color: #409eff; }

.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 22px;
  transition: background 0.2s;
}
.user-area:hover { background: #f2f3f5; }

.username { font-size: 14px; color: #1d2129; font-weight: 500; }
.badge { margin-left: 2px; }

.main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 24px auto;
  padding: 0 24px;
}

.footer {
  border-top: 1px solid #e5e6eb;
  padding: 20px 24px;
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #86909c;
  font-size: 13px;
}

.back-top {
  position: fixed;
  right: 24px;
  bottom: 32px;
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  cursor: pointer;
  color: #4e5969;
  transition: all 0.3s;
  z-index: 50;
}
.back-top:hover {
  color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64,158,255,0.2);
}

@media (max-width: 768px) {
  .header-inner { padding: 0 16px; }
  .main { padding: 0 12px; margin: 16px auto; }
  .footer-inner { flex-direction: column; gap: 8px; text-align: center; }
  .logo-text { display: none; }
  .username { display: none; }
  .nav-link { display: none; }
  .footer-tech { display: none; }
}
</style>
