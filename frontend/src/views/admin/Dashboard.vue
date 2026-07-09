<template>
  <div class="dashboard">
    <h2>仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" :style="{color:stat.color}">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboard } from '@/api/admin'

const stats = ref([
  { label: '总用户', value: 0, color: '#409eff' },
  { label: '总帖子', value: 0, color: '#67c23a' },
  { label: '总评论', value: 0, color: '#e6a23c' },
  { label: '在线用户', value: 0, color: '#f56c6c' },
  { label: '今日新用户', value: 0, color: '#909399' },
  { label: '今日新帖', value: 0, color: '#909399' },
  { label: '今日新评论', value: 0, color: '#909399' },
  { label: '今日浏览', value: 0, color: '#909399' },
])

onMounted(async () => {
  try {
    const res = await getDashboard()
    const d = res.data
    stats.value[0].value = d.totalUsers || 0
    stats.value[1].value = d.totalPosts || 0
    stats.value[2].value = d.totalComments || 0
    stats.value[3].value = d.onlineUsers || 0
    stats.value[4].value = d.todayNewUsers || 0
    stats.value[5].value = d.todayNewPosts || 0
    stats.value[6].value = d.todayNewComments || 0
    stats.value[7].value = d.todayViews || 0
  } catch { /* ignore */ }
})
</script>

<style scoped>
.dashboard h2 { font-size: 20px; margin-bottom: 20px; }

.stat-card { text-align: center; padding: 10px; cursor: pointer; }

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label { font-size: 14px; color: #909399; }
</style>
