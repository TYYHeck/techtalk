<template>
  <div class="post-create">
    <h2><el-icon><EditPen /></el-icon> 发布帖子</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="分类" prop="categoryIds">
        <div class="category-tags-row">
          <el-tag
            v-for="cat in selectedCategories" :key="cat.id"
            closable type="primary" size="large"
            @close="removeCategory(cat.id)"
            class="cat-tag-item"
          >
            {{ cat.icon }} {{ cat.name }}
          </el-tag>
          <el-select
            v-model="pendingCat"
            placeholder="添加分类标签..."
            size="large"
            class="cat-add-select"
            @change="addCategory"
            :popper-append-to-body="false"
          >
            <el-option
              v-for="cat in availableCategories"
              :key="cat.id"
              :label="cat.icon + ' ' + cat.name"
              :value="cat.id"
            />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入帖子标题（2-100字）" maxlength="100" show-word-limit size="large" />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <div class="editor-wrapper">
          <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" mode="default" class="editor-toolbar" />
          <Editor :defaultConfig="editorConfig" mode="default" v-model="form.content" @onCreated="handleCreated" class="editor-body" />
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" @click="submit" :loading="submitting">
          <el-icon><Upload /></el-icon> 发布帖子
        </el-button>
        <el-button size="large" @click="$router.push('/')">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, shallowRef, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost } from '@/api/post'
import { getCategories } from '@/api/category'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const router = useRouter()
const formRef = ref()
const categories = ref([])
const submitting = ref(false)
const editorRef = shallowRef()
const pendingCat = ref(null)

const toolbarConfig = { excludeKeys: ['group-video', 'fullScreen'] }
const editorConfig = {
  placeholder: '请输入内容，支持 Markdown 与富文本格式...',
  MENU_CONF: {
    uploadImage: { server: '/api/upload/image', fieldName: 'file', maxFileSize: 10 * 1024 * 1024 },
  },
}

const form = ref({ categoryIds: [], title: '', content: '' })

// 已选分类的完整信息
const selectedCategories = ref([])

// 可选分类（排除已选的）
const availableCategories = computed(() => {
  const selectedIds = selectedCategories.value.map(c => c.id)
  return (categories.value || []).filter(c => !selectedIds.includes(c.id))
})

function addCategory(catId) {
  if (!catId) return
  const cat = (categories.value || []).find(c => c.id === catId)
  if (cat && !selectedCategories.value.find(c => c.id === catId)) {
    selectedCategories.value.push({ ...cat })
    form.value.categoryIds = selectedCategories.value.map(c => c.id)
  }
  pendingCat.value = null
}

function removeCategory(catId) {
  selectedCategories.value = selectedCategories.value.filter(c => c.id !== catId)
  form.value.categoryIds = selectedCategories.value.map(c => c.id)
}

const rules = {
  categoryIds: [
    {
      validator: (_rule, value, callback) => {
        if (!value || value.length === 0) callback(new Error('请至少选择一个分类'))
        else callback()
      },
      trigger: 'change',
    },
  ],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度 2-100 字', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        const text = value.replace(/<[^>]+>/g, '').trim()
        if (text.length < 10) callback(new Error('内容至少 10 个字符'))
        else if (text.length > 50000) callback(new Error('内容不能超过 50000 字'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

const summary = computed(() => {
  const text = form.value.content.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
  return text.length > 200 ? text.substring(0, 200) + '...' : text
})

onMounted(async () => {
  try { categories.value = await getCategories() || [] } catch { /* ignore */ }
})

function handleCreated(editor) { editorRef.value = editor }

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
    const categoryIds = form.value.categoryIds
    const payload = {
      title: form.value.title,
      content: form.value.content,
      summary: summary.value,
      categoryId: categoryIds.length > 0 ? categoryIds[0] : null,
      categoryIds: categoryIds,
    }
    const res = await createPost(payload)
    ElMessage.success('发布成功！')
    router.push(`/post/${res.data?.id}`)
  } catch { /* handled */ }
  finally { submitting.value = false }
}
</script>

<style scoped>
.post-create {
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 28px 32px;
  border: 1px solid #ebeef5;
}

h2 {
  font-size: 20px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ===== 多分类标签行 ===== */
.category-tags-row {
  display: flex; align-items: center; flex-wrap: wrap; gap: 8px;
}
.cat-tag-item { font-size: 14px; padding: 0 12px; }
.cat-add-select { width: 200px; flex-shrink: 0; }

.editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  transition: border-color 0.2s;
}
.editor-wrapper:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64,158,255,0.15);
}
.editor-toolbar { border-bottom: 1px solid #ebeef5; background: #fafafa; }
.editor-body { min-height: 400px; }

@media (max-width: 768px) {
  .post-create { padding: 20px 16px; }
  .cat-add-select { width: 160px; }
}
</style>
