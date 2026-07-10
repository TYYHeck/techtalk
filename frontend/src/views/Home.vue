<template>
  <div class="home">
    <!-- Sticky Toolbar: Category (left) + Search (right) -->
    <div class="toolbar">
      <el-button size="large" @click="drawerVisible = true" class="category-btn">
        <el-icon><Menu /></el-icon> 分类
      </el-button>
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索感兴趣的技术话题..." clearable @clear="search" @keyup.enter="search" size="large" class="search-input">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" @click="search" round class="search-btn-desktop">搜索</el-button>
      </div>
    </div>

    <!-- Category Drawer -->
    <el-drawer v-model="drawerVisible" direction="ltr" size="280px" title="全部分类" :with-header="true" :z-index="90">
      <div class="drawer-cats">
        <div
          class="drawer-cat-item"
          :class="{ active: !activeCategory }"
          @click="selectCategory(null)"
        >
          <span class="cat-name">全部</span>
          <el-tag size="small" round>{{ totalCount }}</el-tag>
        </div>
        <div
          v-for="cat in categories" :key="cat.id"
          class="drawer-cat-item"
          :class="{ active: activeCategory === cat.id }"
          @click="selectCategory(cat.id)"
        >
          <span class="cat-icon">{{ cat.icon || '📁' }}</span>
          <span class="cat-name">{{ cat.name }}</span>
          <el-tag size="small" round>{{ cat.postCount || 0 }}</el-tag>
        </div>
      </div>
    </el-drawer>

    <!-- Active category tag -->
    <div class="active-filter" v-if="activeCategory && activeCatName">
      <span class="filter-label">当前分类：</span>
      <el-tag closable @close="selectCategory(null)" size="large" type="primary">{{ activeCatName }}</el-tag>
    </div>

    <div class="content">
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
          <el-button type="primary" round @click="$router.push('/create')">发布第一篇文章</el-button>
        </el-empty>
      </div>

      <!-- Post List -->
      <div v-else class="post-list">
        <article v-for="post in posts" :key="post.id" class="post-card" @click="goPost(post.id)">
          <div class="post-tags">
            <el-tag v-if="post.isPinned" type="danger" size="small" effect="dark" round>置顶</el-tag>
            <el-tag v-if="post.isFeatured" type="warning" size="small" effect="dark" round>精华</el-tag>
            <span class="post-category" v-if="post.categoryName">{{ post.categoryName }}</span>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-summary">{{ post.summary }}</p>
          <div class="post-meta">
            <div class="author" @click.stop="$router.push(`/user/${post.author?.id}`)">
              <el-avatar :size="22" :src="post.author?.avatar" />
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
const drawerVisible = ref(false)

const totalCount = computed(() => categories.value.reduce((sum, c) => sum + (c.postCount || 0), 0))

const activeCatName = computed(() => {
  const cat = categories.value.find(c => c.id === activeCategory.value)
  return cat ? cat.name : ''
})

onMounted(async () => {
  await Promise.all([fetchPosts(), fetchCategories()])
})

async function fetchPosts() {
  loading.value = true
  try {
    const res = await getPostList({
      page: page.value, size: size.value,
      categoryId: activeCategory.value || undefined,
      keyword: keyword.value || undefined,
    })
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function fetchCategories() {
  try { categories.value = await getCategories() || [] } catch { /* ignore */ }
}

function selectCategory(id) {
  activeCategory.value = id || null
  page.value = 1
  drawerVisible.value = false
  fetchPosts()
}

function search() { page.value = 1; fetchPosts() }
function goPost(id) { router.push(`/post/${id}`) }

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
/* ===== Toolbar (sticky) ===== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: -20px -24px 20px;
  padding: 12px 24px;
  position: sticky;
  top: 56px;
  z-index: 50;
  background: #f5f6fa;
}
.search-bar {
  display: flex;
  gap: 10px;
  flex: 1;
  min-width: 0;
}
.search-input { max-width: 500px; }
.search-btn-desktop { display: inline-flex; }

.category-btn { display: inline-flex; flex-shrink: 0; }

/* ===== Drawer Categories ===== */
.drawer-cats { display: flex; flex-direction: column; gap: 2px; }
.drawer-cat-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 14px; border-radius: 8px; cursor: pointer;
  transition: all 0.15s; font-size: 14px; color: #303133;
}
.drawer-cat-item:hover { background: #f2f3f5; }
.drawer-cat-item.active { background: rgba(64,158,255,0.08); color: #409eff; font-weight: 500; }
.drawer-cat-item .cat-icon { font-size: 18px; }
.drawer-cat-item .cat-name { flex: 1; }

/* ===== Active Filter ===== */
.active-filter { margin-bottom: 16px; display: flex; align-items: center; gap: 8px; font-size: 14px; }
.filter-label { color: #909399; }

/* ===== Post Card ===== */
.post-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  margin-bottom: 12px;
  cursor: pointer;
  border: 1px solid #ebeef5;
  transition: all 0.2s;
}
.post-card:hover {
  border-color: #c6e2ff;
  box-shadow: 0 2px 12px rgba(64,158,255,0.06);
}

.post-tags { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.post-category { font-size: 12px; color: #909399; background: #f2f3f5; padding: 2px 10px; border-radius: 10px; }

.post-title {
  font-size: 18px; font-weight: 600; margin-bottom: 8px; color: #303133;
  line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.post-summary {
  color: #909399; font-size: 14px; line-height: 1.6; margin-bottom: 14px;
  overflow: hidden; text-overflow: ellipsis; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}

.post-meta {
  display: flex; align-items: center; gap: 16px;
  font-size: 13px; color: #909399; flex-wrap: wrap;
}
.author { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.author:hover .author-name { color: #409eff; }
.author-name { font-weight: 500; color: #606266; transition: color 0.15s; }
.stats { display: flex; gap: 14px; }
.stat-item { display: flex; align-items: center; gap: 3px; }
.time { margin-left: auto; }

/* ===== Skeleton / Empty / Pagination ===== */
.loading-area { display: flex; flex-direction: column; gap: 12px; }
.skeleton-card { background: #fff; border-radius: 8px; padding: 24px; }
.sk-title { height: 22px; width: 60%; border-radius: 4px; margin-bottom: 14px; }
.sk-line { height: 14px; border-radius: 4px; margin-bottom: 10px; }
.sk-meta { height: 18px; width: 35%; border-radius: 4px; margin-top: 14px; }

.empty-state { background: #fff; border-radius: 8px; padding: 60px; }
.pagination { display: flex; justify-content: center; margin-top: 28px; }

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .toolbar { margin: -14px -12px 14px; padding: 10px 12px; }
  .search-input { max-width: 100%; }
  .search-btn-desktop { display: none; }
  .post-card { padding: 16px; }
  .post-title { font-size: 16px; }
  .time { display: none; }
  .stats { gap: 10px; }
}
</style>
