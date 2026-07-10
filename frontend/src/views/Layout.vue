<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <div class="header-left">
          <!-- Mobile menu toggle -->
          <span class="menu-toggle" @click="showMobileMenu = !showMobileMenu">
            <el-icon :size="22"><component :is="showMobileMenu ? 'Close' : 'Menu'" /></el-icon>
          </span>
          <router-link to="/" class="logo">
            <span class="logo-icon">💬</span>
            <span class="logo-text">TechTalk</span>
          </router-link>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-item">首页</router-link>
          <template v-if="authStore.isLoggedIn">
            <router-link to="/create" class="nav-item">
              <el-button type="primary" size="default" round>发布</el-button>
            </router-link>
            <el-dropdown @command="handleCommand" trigger="click" placement="bottom-end">
              <span class="user-area">
                <el-avatar :size="32" :src="authStore.user?.avatar" />
                <span class="username">{{ authStore.user?.username }}</span>
                <el-badge v-if="notifStore.unreadCount > 0" :value="notifStore.unreadCount" :max="99" />
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

      <!-- Mobile menu overlay -->
      <transition name="slide-down">
        <div v-if="showMobileMenu" class="mobile-menu" @click.self="showMobileMenu = false">
          <div class="mobile-menu-inner">
            <router-link to="/" class="mobile-item" @click="showMobileMenu = false">首页</router-link>
            <template v-if="authStore.isLoggedIn">
              <router-link to="/create" class="mobile-item" @click="showMobileMenu = false">发布帖子</router-link>
              <router-link to="/profile" class="mobile-item" @click="showMobileMenu = false">个人中心</router-link>
              <router-link to="/favorites" class="mobile-item" @click="showMobileMenu = false">我的收藏</router-link>
              <router-link to="/notifications" class="mobile-item" @click="showMobileMenu = false">消息通知</router-link>
              <router-link v-if="authStore.isAdmin" to="/admin" class="mobile-item" @click="showMobileMenu = false">管理后台</router-link>
              <span class="mobile-item" @click="handleCommand('logout'); showMobileMenu = false">退出登录</span>
            </template>
            <template v-else>
              <router-link to="/login" class="mobile-item" @click="showMobileMenu = false">登录</router-link>
              <router-link to="/register" class="mobile-item" @click="showMobileMenu = false">注册</router-link>
            </template>
          </div>
        </div>
      </transition>
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

const showMobileMenu = ref(false)
let notifTimer = null
const showBackTop = ref(false)

function startNotifPolling() {
  if (authStore.isLoggedIn) {
    notifStore.fetchUnreadCount()
    notifTimer = setInterval(() => {
      if (!authStore.isLoggedIn) { clearInterval(notifTimer); notifTimer = null; return }
      notifStore.fetchUnreadCount()
    }, 30000)
  }
}

function onScroll() { showBackTop.value = window.scrollY > 400 }
function scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }

onMounted(() => {
  startNotifPolling()
  window.addEventListener('scroll', onScroll)
})

onUnmounted(() => {
  if (notifTimer) { clearInterval(notifTimer); notifTimer = null }
  window.removeEventListener('scroll', onScroll)
})

function handleCommand(cmd) {
  switch (cmd) {
    case 'profile': router.push('/profile'); break
    case 'favorites': router.push('/favorites'); break
    case 'notifications': router.push('/notifications'); break
    case 'admin': router.push('/admin'); break
    case 'logout': authStore.logout(); router.push('/'); break
  }
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }

/* ===== Header ===== */
.header {
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
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
  height: 56px;
}

.header-left { display: flex; align-items: center; gap: 12px; }

.menu-toggle {
  display: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  color: #303133;
}
.menu-toggle:hover { background: #f2f3f5; }

.logo { display: flex; align-items: center; gap: 8px; color: #303133; font-size: 20px; font-weight: 700; }
.logo-icon { font-size: 24px; }
.logo-text { color: #409eff; }

.nav { display: flex; align-items: center; gap: 4px; }
.nav-item { color: #606266; font-size: 14px; padding: 6px 14px; border-radius: 6px; transition: all 0.2s; }
.nav-item:hover { color: #409eff; background: rgba(64,158,255,0.06); }
.nav-link { color: #606266; font-size: 14px; padding: 6px 14px; border-radius: 6px; transition: all 0.2s; }
.nav-link:hover { color: #409eff; }

.user-area {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 8px; border-radius: 20px; transition: background 0.2s;
}
.user-area:hover { background: #f2f3f5; }
.username { font-size: 14px; color: #303133; font-weight: 500; }

/* ===== Main ===== */
.main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 20px auto;
  padding: 0 24px;
}

/* ===== Footer ===== */
.footer { border-top: 1px solid #e5e6eb; padding: 16px 24px; }
.footer-inner { max-width: 1200px; margin: 0 auto; text-align: center; color: #909399; font-size: 13px; }

/* ===== Back to top ===== */
.back-top {
  position: fixed; right: 20px; bottom: 28px;
  width: 40px; height: 40px; background: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08); cursor: pointer;
  color: #606266; transition: all 0.3s; z-index: 50;
}
.back-top:hover { color: #409eff; transform: translateY(-2px); }

/* ===== Mobile Menu ===== */
.mobile-menu {
  position: fixed; inset: 0; top: 56px; background: rgba(0,0,0,0.4); z-index: 99;
}
.mobile-menu-inner {
  background: #fff; padding: 8px 16px;
  display: flex; flex-direction: column; gap: 2px;
}
.mobile-item {
  display: block; padding: 12px 16px; border-radius: 8px;
  font-size: 15px; color: #303133; cursor: pointer; transition: background 0.15s;
}
.mobile-item:hover { background: #f2f3f5; color: #409eff; }

/* ===== Transitions ===== */
.slide-down-enter-active { animation: slideDown 0.25s ease-out; }
.slide-down-leave-active { animation: slideDown 0.2s ease-in reverse; }
@keyframes slideDown { from { opacity: 0; transform: translateY(-8px); } to { opacity: 1; transform: translateY(0); } }

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .header-inner { padding: 0 16px; }
  .menu-toggle { display: block; }
  .main { padding: 0 12px; margin: 14px auto; }
  .username { display: none; }
  .nav-link { display: none; }
}
</style>
