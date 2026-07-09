<template>
  <div class="post-create">
    <h2>发布帖子</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="选择分类">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.icon + ' ' + cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入帖子标题（2-100字）" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="15" placeholder="支持 Markdown 格式..."
          maxlength="50000" show-word-limit />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit" :loading="submitting">发布帖子</el-button>
        <el-button @click="$router.push('/')">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost } from '@/api/post'
import { getCategories } from '@/api/category'

const router = useRouter()
const formRef = ref()
const categories = ref([])
const submitting = ref(false)

const form = ref({
  categoryId: null,
  title: '',
  content: '',
})

const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度 2-100 字', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, max: 50000, message: '内容长度 10-50000 字', trigger: 'blur' }
  ],
}

const summary = computed(() => {
  const text = form.value.content.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
  return text.length > 200 ? text.substring(0, 200) + '...' : text
})

onMounted(async () => {
  try {
    const res = await getCategories()
    categories.value = res || []
  } catch { /* ignore */ }
})

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const res = await createPost({
      ...form.value,
      summary: summary.value,
    })
    ElMessage.success('发布成功！')
    router.push(`/post/${res.data?.id}`)
  } catch { /* handled */ }
  finally { submitting.value = false }
}
</script>

<style scoped>
.post-create {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
}

h2 {
  font-size: 22px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
}
</style>
