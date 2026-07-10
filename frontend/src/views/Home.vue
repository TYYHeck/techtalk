<template>
  <div class="home">
    <aside class="sidebar">
      <div class="category-card">
        <h3><el-icon><Grid /></el-icon> 分类导航</h3>
        <el-menu :default-active="String(activeCategory || '')" @select="handleCategorySelect">
          <el-menu-item index="">
            <el-icon><Collection /></el-icon>
            <span>全部</span>
            <el-tag size="small" type="info" round>{{ totalCount }}</el-tag>
          </el-menu-item>
          <el-menu-item v-for="cat in categories" :key="cat.id" :index="String(cat.id)">
            <span class="cat-icon">{{ cat.icon || '📁' }}</span>
            <span>{{ cat.name }}</span>
            <el-tag size="small" type="info" round>{{ cat.postCount }}</el-tag>
          </el-menu-item>
        </el-menu>
      </div>
    </aside>
    <div class="content">
      <!-- Search Bar -->
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索感兴趣的技术话题..." clearable @clear="search" @keyup.enter="search" class="search-input" size="large">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" @click="search" round>
          <el-icon><Search /></el-icon> 搜索
        </el-button>
      </div>

      <!-- Skeleton Loading -->
      <div v-if="loading" class="loading-area">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <div class="skeleton-shimmer sk-title"></div>
          <div class="skeleton-shimmer sk-line" style="width:70%"></div>
          <div class="skeleton-shimmer sk-line" style="width:50%"></div>
          <div class="skeleton-shimmer sk-meta"></div>
        </div>
      </div>

      <!-- Empty -->
      <div v-else-if="posts.length === 0" class="empty-state">
        <el-empty description="暂无帖子">
          <template #image>
            <el-icon :size="64" color="#c9cdd4"><Document /></el-icon>
          </template>
          <el-button type="primary" round @click="$router.push('/create')">发布第一篇文章</el-button>
        </el-empty>
      </div>

      <!-- Post List -->
      <div v-else class="post-list">
        <article v-for="post in posts" :key="post.id" class="post-card card-hover" @click="goPost(post.id)">
          <div class="post-tags">
            <el-tag v-if="post.isPinned" type="danger" size="small" effect="dark" round>置顶</el-tag>
            <el-tag v-if="post.isFeatured" type="warning" size="small" effect="dark" round>精华</el-tag>
            <span class="post-category" v-if="post.categoryName">{{ post.categoryName }}</span>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-summary">{{ post.summary }}</p>
          <div class="post-meta">
            <div class="author">
              <el-avatar :size="24" :src="post.author?.avatar" />
              <span class="author-name">{{ post.author?.username }}</span>
            </div>
            <div class="stats">
              <span class="stat-item"><el-icon><View /></el-icon> {{ post.viewCount }}</span>
              <span class="stat-item"><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
              <span class="stat-item"><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
            </div>
            <span class="time">{{ formatTime(post.createdAt) }}</span>
          </div>
        </article>
      </div>

      <!-- Pagination -->
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
import { ref, computed, onMounted } from 'vue'
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

const totalCount = computed(() => {
  return categories.value.reduce((sum, c) => sum + (c.postCount || 0), 0)
})

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
  if (!time) return ''
  const d = dayjs(time)
  if (!d.isValid()) return ''
  const now = dayjs()
  const diffMin = now.diff(d, 'minute')
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin} 分钟前`
  const diffHour = now.diff(d, 'hour')
  if (diffHour < 24) return `${diffHour} 小时前`
  const diffDay = now.diff(d, 'day')
  if (diffDay < 7) return `${diffDay} 天前`
  return d.format('YYYY-MM-DD')
}

</script>

<style scoped>
.home {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* ===== Sidebar ===== */
.sidebar {
  width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: 84px;
}

.category-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.category-card h3 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.category-card :deep(.el-menu-item) {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  border-radius: 8px;
  margin: 2px 0;
  padding: 0 12px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.category-card :deep(.el-menu-item.is-active) {
  background: rgba(64,158,255,0.08);
  color: var(--primary);
  font-weight: 500;
}
.category-card :deep(.el-menu-item .el-tag) {
  margin-left: auto;
}

.cat-icon { font-size: 16px; }

/* ===== Content ===== */
.content { flex: 1; min-width: 0; }

/* ===== Search ===== */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.search-input { max-width: 480px; }

/* ===== Post Card ===== */
.post-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 22px 24px;
  margin-bottom: 14px;
  cursor: pointer;
  border: 1px solid var(--border-light);
  transition: all var(--transition);
}
.post-card:hover {
  border-color: var(--primary-light);
  box-shadow: 0 4px 20px rgba(64,158,255,0.08);
}

.post-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.post-category {
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-page);
  padding: 2px 10px;
  border-radius: 10px;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-summary {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.65;
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
  gap: 18px;
  font-size: 13px;
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
}
.author-name { font-weight: 500; color: var(--text-regular); }

.stats { display: flex; gap: 16px; }
.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.time { margin-left: auto; color: var(--text-placeholder); }

/* ===== Skeleton ===== */
.loading-area { display: flex; flex-direction: column; gap: 14px; }
.skeleton-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 24px;
}
.sk-title { height: 22px; width: 60%; border-radius: 4px; margin-bottom: 14px; }
.sk-line { height: 14px; border-radius: 4px; margin-bottom: 10px; }
.sk-meta { height: 18px; width: 35%; border-radius: 4px; margin-top: 14px; }

/* ===== Empty ===== */
.empty-state {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 60px;
}

/* ===== Pagination ===== */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .home { flex-direction: column; }
  .sidebar { width: 100%; position: static; }
  .search-bar { flex-direction: column; }
  .search-input { max-width: 100%; }
  .post-card { padding: 16px; }
  .post-title { font-size: 16px; }
  .time { display: none; }
}
</style>
