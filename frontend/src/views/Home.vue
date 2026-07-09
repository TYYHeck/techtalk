<template>
  <div class="home">
    <div class="sidebar">
      <div class="category-card">
        <h3>分类导航</h3>
        <el-menu :default-active="String(activeCategory || '')" @select="handleCategorySelect">
          <el-menu-item index="">
            <el-icon><Collection /></el-icon>
            <span>全部</span>
          </el-menu-item>
          <el-menu-item v-for="cat in categories" :key="cat.id" :index="String(cat.id)">
            <span>{{ cat.icon || '📁' }}</span>
            <span style="margin-left:8px">{{ cat.name }}</span>
            <el-tag size="small" type="info" style="margin-left:auto">{{ cat.postCount }}</el-tag>
          </el-menu-item>
        </el-menu>
      </div>
    </div>
    <div class="content">
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索帖子..." clearable @clear="search" @keyup.enter="search" class="search-input">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="search">搜索</el-button>
      </div>

      <div v-if="loading" class="loading-area">
        <el-skeleton :rows="3" animated v-for="i in 3" :key="i" style="margin-bottom:20px" />
      </div>

      <div v-else-if="posts.length === 0" class="empty">
        <el-empty description="暂无帖子，快来发布第一个吧！" />
      </div>

      <div v-else class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-card" @click="goPost(post.id)">
          <div class="post-header">
            <el-tag v-if="post.isPinned" type="danger" size="small" effect="dark">置顶</el-tag>
            <el-tag v-if="post.isFeatured" type="warning" size="small" effect="dark" style="margin-left:4px">精华</el-tag>
            <span class="post-category" v-if="post.categoryName">{{ post.categoryName }}</span>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-summary">{{ post.summary }}</p>
          <div class="post-meta">
            <div class="author">
              <el-avatar :size="22" :src="post.author?.avatar" />
              <span>{{ post.author?.username }}</span>
            </div>
            <div class="stats">
              <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
              <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
            </div>
            <span class="time">{{ formatTime(post.createdAt) }}</span>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="total > size">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchPosts" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList } from '@/api/post'
import { getCategories } from '@/api/category'
import dayjs from 'dayjs'

const router = useRouter()
const posts = ref([])
const categories = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeCategory = ref(null)
const keyword = ref('')
const loading = ref(true)

onMounted(async () => {
  await Promise.all([fetchPosts(), fetchCategories()])
})

async function fetchPosts() {
  loading.value = true
  try {
    const res = await getPostList({
      page: page.value,
      size: size.value,
      categoryId: activeCategory.value || undefined,
      keyword: keyword.value || undefined,
    })
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res = await getCategories()
    categories.value = res || []
  } catch { /* ignore */ }
}

function handleCategorySelect(index) {
  activeCategory.value = index ? Number(index) : null
  page.value = 1
  fetchPosts()
}

function search() {
  page.value = 1
  fetchPosts()
}

function goPost(id) {
  router.push(`/post/${id}`)
}

function formatTime(time) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.home {
  display: flex;
  gap: 20px;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
}

.category-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  position: sticky;
  top: 80px;
}

.category-card h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #303133;
}

.category-card .el-menu {
  border-right: none;
  max-height: 60vh;
  overflow-y: auto;
}

.content {
  flex: 1;
  min-width: 0;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input { max-width: 400px; }

.post-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  cursor: pointer;
  border: 1px solid #ebeef5;
  transition: all 0.2s;
}

.post-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64,158,255,0.1);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.post-category {
  font-size: 12px;
  color: #909399;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.post-title {
  font-size: 18px;
  margin-bottom: 8px;
  color: #303133;
  line-height: 1.4;
}

.post-summary {
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 13px;
  color: #909399;
  flex-wrap: wrap;
}

.author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stats {
  display: flex;
  gap: 14px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.time { margin-left: auto; }

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.loading-area, .empty {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
}

@media (max-width: 768px) {
  .home { flex-direction: column; }
  .sidebar { width: 100%; }
}
</style>
