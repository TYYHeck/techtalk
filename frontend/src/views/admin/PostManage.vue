<template>
  <div class="manage">
    <h2>帖子管理</h2>
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="fetchData" style="width:140px">
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="审核中" value="AUDITING" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-input v-model="keyword" placeholder="搜索标题" clearable @clear="fetchData" @keyup.enter="fetchData"
        style="width:260px" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>

    <el-table :data="posts" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isPinned" label="置顶" width="70">
        <template #default="{row}">{{ row.isPinned ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="isFeatured" label="精华" width="70">
        <template #default="{row}">{{ row.isFeatured ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="{row}">
          <el-button size="small" @click="togglePin(row)">
            {{ row.isPinned ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button size="small" @click="toggleFeature(row)">
            {{ row.isFeatured ? '取消精华' : '精华' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="total"
      layout="prev, pager, next" background style="margin-top:20px;justify-content:center"
      @current-change="fetchData" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminPosts, togglePostPin, togglePostFeature, updatePostStatus, adminDeletePost } from '@/api/admin'

const posts = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const statusFilter = ref('')
const loading = ref(false)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminPosts({
      page: page.value, size: size.value,
      keyword: keyword.value, status: statusFilter.value || undefined
    })
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function statusType(status) {
  return status === 'PUBLISHED' ? 'success' : status === 'AUDITING' ? 'warning' : 'danger'
}

async function togglePin(row) {
  await togglePostPin(row.id)
  ElMessage.success('操作成功')
  fetchData()
}

async function toggleFeature(row) {
  await togglePostFeature(row.id)
  ElMessage.success('操作成功')
  fetchData()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除此帖子？', '警告', { type: 'error' })
  } catch { return }
  await adminDeletePost(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.manage h2 { font-size: 20px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
