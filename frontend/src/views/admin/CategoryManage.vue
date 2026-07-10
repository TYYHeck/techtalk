<template>
  <div class="manage">
    <h2>分类管理</h2>
    <div class="toolbar">
      <el-button type="primary" @click="showCreateDialog">新增分类</el-button>
    </div>

    <el-table :data="categories" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="icon" label="图标" width="80" />
      <el-table-column prop="name" label="名称" width="140" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="postCount" label="帖子数" width="80" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button size="small" @click="editCategory(row)">编辑</el-button>
          <el-popconfirm title="确定删除此分类？" @confirm="handleDelete(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑对话框 -->
    <el-dialog :title="editing ? '编辑分类' : '新增分类'" v-model="dialogVisible" width="450px">
      <el-form :model="form" label-position="top">
        <el-form-item label="图标">
          <el-select v-model="form.icon" placeholder="选择图标" filterable>
            <el-option v-for="emoji in emojiList" :key="emoji" :label="emoji" :value="emoji" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="分类描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'

const emojiList = [
  '📁', '💬', '🎨', '⚛️', '🔧', '📱', '🤖', '🧠',
  '🔒', '📊', '🎮', '☁️', '🛠️', '🚀', '💡', '🎯',
  '📚', '🐍', '☕', '🦀', '🐹', '🐘', '🐳', '⭐',
  '🔥', '💻', '🌐', '📝', '🎵', '📷', '🏆', '💎',
  '🎓', '💼', '🏠', '🌟', '❤️', '👍', '✨', '🎉',
  '🌿', '🍀', '🌈', '⚡', '🎪', '🎬', '📌', '🏷️',
]

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const form = ref({ name: '', description: '', icon: '📁', sortOrder: 0 })

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    categories.value = await getAllCategories() || []
  } finally { loading.value = false }
}

function showCreateDialog() {
  editing.value = false
  editingId.value = null
  form.value = { name: '', description: '', icon: '📁', sortOrder: 0 }
  dialogVisible.value = true
}

function editCategory(row) {
  editing.value = true
  editingId.value = row.id
  form.value = {
    name: row.name,
    description: row.description || '',
    icon: row.icon || '📁',
    sortOrder: row.sortOrder || 0,
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.name) { ElMessage.warning('请输入名称'); return }
  submitting.value = true
  try {
    if (editing.value) {
      await updateCategory(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createCategory(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch { /* handled */ }
  finally { submitting.value = false }
}

async function handleDelete(row) {
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.manage h2 { font-size: 20px; margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
</style>
