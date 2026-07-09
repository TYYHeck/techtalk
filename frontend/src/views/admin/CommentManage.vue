<template>
  <div class="manage">
    <h2>评论管理</h2>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索评论内容" clearable @clear="fetchData" @keyup.enter="fetchData"
        style="width:300px" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>

    <el-table :data="comments" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="内容" min-width="200">
        <template #default="{row}">
          <div>{{ row.content || '-' }}</div>
          <div style="font-size:12px;color:#909399;margin-top:4px">
            用户: {{ row.user_username }} | 帖子: {{ row.post_title }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{row}">
          <el-popconfirm title="确定删除此评论？" @confirm="handleDelete(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
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
import { ElMessage } from 'element-plus'
import { getAdminComments, adminDeleteComment } from '@/api/admin'

const comments = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const loading = ref(false)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminComments({ page: page.value, size: size.value, keyword: keyword.value })
    comments.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleDelete(row) {
  await adminDeleteComment(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.manage h2 { font-size: 20px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
