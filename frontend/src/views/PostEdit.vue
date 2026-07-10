<template>
  <div class="post-edit" v-loading="loading">
    <h2><el-icon><Edit /></el-icon> 编辑帖子</h2>
    <el-form v-if="post" :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="选择分类" size="large">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.icon + ' ' + cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit size="large" />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <div class="editor-wrapper">
          <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" mode="default" class="editor-toolbar" />
          <Editor :defaultConfig="editorConfig" mode="default" v-model="form.content" @onCreated="handleCreated" class="editor-body" />
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" @click="submit" :loading="submitting">
          <el-icon><Check /></el-icon> 保存修改
        </el-button>
        <el-button size="large" @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, shallowRef, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPostById, updatePost } from '@/api/post'
import { getCategories } from '@/api/category'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const post = ref(null)
const categories = ref([])
const loading = ref(true)
const submitting = ref(false)

const editorRef = shallowRef()

const toolbarConfig = {
  excludeKeys: ['group-video', 'fullScreen'],
}

const editorConfig = {
  placeholder: '编辑帖子内容...',
  MENU_CONF: {
    uploadImage: {
      server: '/api/upload/image',
      fieldName: 'file',
      maxFileSize: 10 * 1024 * 1024,
    },
  },
}

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
    {
      validator: (_rule, value, callback) => {
        const text = value.replace(/<[^>]+>/g, '').trim()
        if (text.length < 10) callback(new Error('内容至少 10 个字符'))
        else callback()
      },
      trigger: 'blur',
    },
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

function handleCreated(editor) {
  editorRef.value = editor
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
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
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

h2 {
  font-size: 22px;
  margin-bottom: 28px;
  padding-bottom: 14px;
  border-bottom: 2px solid #f56c6c;
  display: flex;
  align-items: center;
  gap: 8px;
}

.editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.3s;
}

.editor-wrapper:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 1px rgba(64,158,255,0.2);
}

.editor-toolbar {
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.editor-body {
  min-height: 400px;
}
</style>
