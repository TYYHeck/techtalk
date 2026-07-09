<template>
  <div class="manage">
    <h2>用户管理</h2>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名/邮箱" clearable @clear="fetchData" @keyup.enter="fetchData"
        style="width:260px" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>

    <el-table :data="users" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{row}">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ row.status === 'ACTIVE' ? '正常' : '封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="postCount" label="帖子" width="70" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button v-if="row.role !== 'ADMIN'" size="small"
            :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
            @click="toggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '封禁' : '解封' }}
          </el-button>
          <el-button v-if="row.role !== 'ADMIN'" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
import { getAdminUsers, updateUserStatus, deleteUser } from '@/api/admin'

const users = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const loading = ref(false)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminUsers({ page: page.value, size: size.value, keyword: keyword.value })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function toggleStatus(row) {
  const newStatus = row.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE'
  await updateUserStatus(row.id, { status: newStatus })
  ElMessage.success(newStatus === 'ACTIVE' ? '已解封' : '已封禁')
  fetchData()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}"？此操作不可撤销！`, '警告', { type: 'error' })
  } catch { return }
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.manage h2 { font-size: 20px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
