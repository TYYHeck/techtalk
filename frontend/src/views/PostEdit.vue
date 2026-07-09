<template>
  <div class="post-edit" v-loading="loading">
    <h2>编辑帖子</h2>
    <el-form v-if="post" :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="选择分类">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.icon + ' ' + cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="15" maxlength="50000" show-word-limit />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit" :loading="submitting">保存修改</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPostById, updatePost } from '@/api/post'
import { getCategories } from '@/api/category'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const post = ref(null)
const categories = ref([])
const loading = ref(true)
const submitting = ref(false)

const form = ref({ categoryId: null, title: '', content: '' })

const summary = computed(() => {
  const text = form.value.content.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
  return text.length > 200 ? text.substring(0, 200) + '...' : text
})

const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度 2-100 字', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, message: '内容至少 10 字', trigger: 'blur' }
  ],
}

onMounted(async () => {
  try {
    const [postRes, catRes] = await Promise.all([
      getPostById(route.params.id),
      getCategories(),
    ])
    post.value = postRes.data
    form.value.categoryId = post.value.categoryId
    form.value.title = post.value.title
    form.value.content = post.value.content
    categories.value = catRes || []
  } catch {
    router.push('/')
  } finally {
    loading.value = false
  }
})

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await updatePost(post.value.id, { ...form.value, summary: summary.value })
    ElMessage.success('修改成功！')
    router.push(`/post/${post.value.id}`)
  } catch { /* handled */ }
  finally { submitting.value = false }
}
</script>

<style scoped>
.post-edit {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
}

h2 { font-size: 22px; margin-bottom: 24px; padding-bottom: 12px; border-bottom: 2px solid #f56c6c; }
</style>
