<template>
  <div class="dashboard">
    <h2>仪表盘</h2>

    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" class="stat-card" :class="stat.gradient">
          <div class="stat-icon"><el-icon :size="28"><component :is="stat.icon" /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">近 7 天数据趋势</span></template>
          <v-chart :option="trendOption" style="height:320px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">内容分布</span></template>
          <v-chart :option="pieOption" style="height:320px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboard } from '@/api/admin'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent,
} from 'echarts/components'

use([
  CanvasRenderer, BarChart, LineChart, PieChart,
  TitleComponent, TooltipComponent, LegendComponent, GridComponent,
])

const stats = ref([
  { label: '总用户', value: 0, icon: 'UserFilled',  gradient: 'grad-blue' },
  { label: '总帖子', value: 0, icon: 'Document',   gradient: 'grad-green' },
  { label: '总评论', value: 0, icon: 'ChatDotRound', gradient: 'grad-orange' },
  { label: '在线用户', value: 0, icon: 'Connection', gradient: 'grad-red' },
  { label: '今日新用户', value: 0, icon: 'User',    gradient: 'grad-purple' },
  { label: '今日新帖', value: 0, icon: 'Edit',      gradient: 'grad-teal' },
  { label: '今日新评论', value: 0, icon: 'ChatLineSquare', gradient: 'grad-pink' },
  { label: '今日浏览', value: 0, icon: 'View',      gradient: 'grad-indigo' },
])

const trendDays = ref([])
const trendPosts = ref([])
const trendUsers = ref([])
const trendComments = ref([])

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['新帖', '新用户', '新评论'], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
  xAxis: { type: 'category', data: trendDays.value, axisLine: { lineStyle: { color: '#dcdfe6' } } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f2f5', type: 'dashed' } } },
  series: [
    { name: '新帖', type: 'bar', data: trendPosts.value, itemStyle: { color: '#409eff', borderRadius: [4,4,0,0] }, barWidth: 20 },
    { name: '新用户', type: 'line', data: trendUsers.value, smooth: true, itemStyle: { color: '#67c23a' }, symbol: 'circle', symbolSize: 6 },
    { name: '新评论', type: 'line', data: trendComments.value, smooth: true, itemStyle: { color: '#e6a23c' }, symbol: 'circle', symbolSize: 6 },
  ],
}))

const pieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', right: '5%', top: 'center' },
  series: [{
    type: 'pie',
    radius: ['50%', '75%'],
    center: ['38%', '50%'],
    avoidLabelOverlap: false,
    label: { show: false },
    emphasis: {
      label: { show: true, fontSize: 16, fontWeight: 'bold' },
      itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' },
    },
    data: [
      { value: 0, name: '用户', itemStyle: { color: '#409eff' } },
      { value: 0, name: '帖子', itemStyle: { color: '#67c23a' } },
      { value: 0, name: '评论', itemStyle: { color: '#e6a23c' } },
    ],
  }],
}))

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

    // Pie data
    const pieSeries = pieOption.value.series[0].data
    pieSeries[0].value = d.totalUsers || 0
    pieSeries[1].value = d.totalPosts || 0
    pieSeries[2].value = d.totalComments || 0

    // Trend data (fallback if backend doesn't return)
    if (d.trendDays) {
      trendDays.value = d.trendDays
      trendPosts.value = d.trendPosts || []
      trendUsers.value = d.trendUsers || []
      trendComments.value = d.trendComments || []
    } else {
      const days = []
      for (let i = 6; i >= 0; i--) {
        const d2 = new Date(); d2.setDate(d2.getDate() - i)
        days.push(`${d2.getMonth()+1}/${d2.getDate()}`)
      }
      trendDays.value = days
      trendPosts.value = [0,0,0,0,0,0, d.todayNewPosts || 0]
      trendUsers.value = [0,0,0,0,0,0, d.todayNewUsers || 0]
      trendComments.value = [0,0,0,0,0,0, d.todayNewComments || 0]
    }
  } catch { /* ignore */ }
})
</script>

<style scoped>
.dashboard h2 {
  font-size: 22px;
  margin-bottom: 24px;
  font-weight: 600;
}

.stat-row { margin-bottom: 24px; }

.stat-card {
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.3s ease;
  border: none;
  overflow: hidden;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 18px;
}

.stat-icon {
  width: 52px; height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info { flex: 1; min-width: 0; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

/* Gradient backgrounds for icons */
.grad-blue   .stat-icon { background: linear-gradient(135deg, #409eff, #337ecc); }
.grad-green  .stat-icon { background: linear-gradient(135deg, #67c23a, #529b2e); }
.grad-orange .stat-icon { background: linear-gradient(135deg, #e6a23c, #cf9236); }
.grad-red    .stat-icon { background: linear-gradient(135deg, #f56c6c, #e04747); }
.grad-purple .stat-icon { background: linear-gradient(135deg, #a855f7, #7c3aed); }
.grad-teal   .stat-icon { background: linear-gradient(135deg, #14b8a6, #0d9488); }
.grad-pink   .stat-icon { background: linear-gradient(135deg, #ec4899, #db2777); }
.grad-indigo .stat-icon { background: linear-gradient(135deg, #6366f1, #4f46e5); }

.chart-row { margin-bottom: 20px; }
.chart-card { border-radius: 10px; border: none; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.chart-title { font-size: 15px; font-weight: 600; color: #303133; }

@media (max-width: 1200px) {
  .chart-row .el-col:first-child,
  .chart-row .el-col:last-child {
    max-width: 100%; flex: 0 0 100%; margin-bottom: 20px;
  }
}
</style>
