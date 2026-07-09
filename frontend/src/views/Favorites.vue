<template>
  <div class="favorites">
    <h2>我的收藏</h2>

    <div v-if="posts.length === 0" class="empty">
      <el-empty description="还没有收藏任何帖子" />
    </div>

    <div v-else class="post-list">
      <div v-for="post in posts" :key="post.id" class="post-card" @click="$router.push(`/post/${post.id}`)">
        <h3>{{ post.title }}</h3>
        <p>{{ post.summary }}</p>
        <div class="meta">
          <span>{{ post.author?.username }}</span>
          <span>{{ dayjs(post.createdAt).format('YYYY-MM-DD') }}</span>
          <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="total > size">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
        layout="prev, pager, next" background @current-change="fetchData" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFavorites } from '@/api/favorite'
import dayjs from 'dayjs'

const posts = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => fetchData())

async function fetchData() {
  try {
    const res = await getFavorites({ page: page.value, size: size.value })
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* */ }
}
</script>

<style scoped>
.favorites { max-width: 700px; margin: 0 auto; }

h2 { font-size: 20px; margin-bottom: 20px; }

.post-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 12px;
  cursor: pointer;
  border: 1px solid #ebeef5;
  transition: all 0.2s;
}

.post-card:hover { border-color: #409eff; }

.post-card h3 { font-size: 17px; margin-bottom: 8px; color: #303133; }
.post-card p { font-size: 14px; color: #909399; margin-bottom: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.meta { display: flex; gap: 16px; font-size: 13px; color: #909399; flex-wrap: wrap; }

.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
