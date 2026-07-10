<template>
  <div class="admin-layout">
    <!-- Mobile toggle -->
    <div class="admin-mobile-bar" v-if="isMobile">
      <span class="admin-toggle" @click="sidebarOpen = !sidebarOpen">
        <el-icon :size="20"><component :is="sidebarOpen ? 'Close' : 'Menu'" /></el-icon>
      </span>
      <span class="admin-title">管理后台</span>
    </div>

    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-header">
        <h2>管理后台</h2>
      </div>
      <el-menu :default-active="activeMenu" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/admin"><el-icon><DataAnalysis /></el-icon> 仪表盘</el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><UserFilled /></el-icon> 用户管理</el-menu-item>
        <el-menu-item index="/admin/posts"><el-icon><Document /></el-icon> 帖子管理</el-menu-item>
        <el-menu-item index="/admin/comments"><el-icon><ChatDotRound /></el-icon> 评论管理</el-menu-item>
        <el-menu-item index="/admin/categories"><el-icon><Collection /></el-icon> 分类管理</el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button text style="color:#bfcbd9" @click="$router.push('/')">
          <el-icon><Back /></el-icon> 返回前台
        </el-button>
      </div>
    </aside>

    <!-- Overlay for mobile -->
    <div v-if="isMobile && sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false"></div>

    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const sidebarOpen = ref(false)
const isMobile = ref(window.innerWidth <= 768)

function onResize() { isMobile.value = window.innerWidth <= 768 }

onMounted(() => { window.addEventListener('resize', onResize) })
onUnmounted(() => { window.removeEventListener('resize', onResize) })

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/admin/users')) return '/admin/users'
  if (path.startsWith('/admin/posts')) return '/admin/posts'
  if (path.startsWith('/admin/comments')) return '/admin/comments'
  if (path.startsWith('/admin/categories')) return '/admin/categories'
  return '/admin'
})
</script>

<style scoped>
.admin-layout {
  position: fixed;
  top: 56px;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: stretch;
}

.admin-mobile-bar {
  display: none;
  background: #304156;
  color: #fff;
  padding: 0 16px;
  height: 48px;
  align-items: center;
  gap: 12px;
  position: fixed;
  top: 56px;
  left: 0;
  right: 0;
  z-index: 70;
}
.admin-toggle { cursor: pointer; padding: 4px; border-radius: 4px; }
.admin-toggle:hover { background: rgba(255,255,255,0.1); }
.admin-title { font-size: 15px; font-weight: 600; }

.sidebar {
  width: 220px;
  background: #304156;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}
.sidebar-header h2 { color: #fff; font-size: 17px; margin: 0; }

.sidebar .el-menu { border-right: none; flex: 1; }
.sidebar :deep(.el-menu-item) { justify-content: center; padding-left: 20px !important; padding-right: 20px !important; }

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  justify-content: center;
}

.sidebar-overlay {
  position: fixed;
  inset: 0;
  top: 56px;
  background: rgba(0,0,0,0.4);
  z-index: 55;
}

.admin-main {
  flex: 1;
  padding: 40px 32px;
  background: #f0f2f5;
  min-width: 0;
  overflow-y: auto;
}

.admin-main :deep(> div) {
  max-width: 1200px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .admin-mobile-bar { display: flex; }
  .sidebar {
    position: fixed;
    left: 0;
    top: 104px;
    bottom: 0;
    z-index: 60;
    transform: translateX(-100%);
  }
  .sidebar.open { transform: translateX(0); }
  .admin-layout { position: fixed; top: 56px; left: 0; right: 0; bottom: 0; }
  .admin-main { padding: 68px 16px 24px; }
  .admin-main :deep(> div) { max-width: none; }
}
</style>
