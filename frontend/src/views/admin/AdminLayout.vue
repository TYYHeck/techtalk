<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>⚙️ 管理后台</h2>
      </div>
      <el-menu :default-active="activeMenu" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon> 仪表盘
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon> 用户管理
        </el-menu-item>
        <el-menu-item index="/admin/posts">
          <el-icon><Document /></el-icon> 帖子管理
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon> 评论管理
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Collection /></el-icon> 分类管理
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button text style="color:#bfcbd9" @click="$router.push('/')">
          <el-icon><Back /></el-icon> 返回前台
        </el-button>
      </div>
    </aside>
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
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
  display: flex;
  min-height: calc(100vh - 60px);
  margin: -20px;
}

.sidebar {
  width: 220px;
  background: #304156;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.sidebar-header h2 {
  color: #fff;
  font-size: 18px;
}

.sidebar .el-menu {
  border-right: none;
  flex: 1;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.admin-main {
  flex: 1;
  padding: 24px;
  background: #f0f2f5;
  min-width: 0;
}
</style>
