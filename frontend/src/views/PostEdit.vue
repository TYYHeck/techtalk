<template>
  <div class="post-edit" v-loading="loading">
    <h2><el-icon><Edit /></el-icon> 编辑帖子</h2>
    <el-form v-if="post" :model="form" :rules="rules" ref="formRef" label-position="top">
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
const pendingCat = ref(null)

const toolbarConfig = { excludeKeys: ['group-video', 'fullScreen'] }
const editorConfig = {
  placeholder: '编辑帖子内容...',
  MENU_CONF: {
    uploadImage: { server: '/api/upload/image', fieldName: 'file', maxFileSize: 10 * 1024 * 1024 },
  },
}

const form = ref({ categoryIds: [], title: '', content: '' })
const selectedCategories = ref([])

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

const summary = computed(() => {
  const text = form.value.content.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
  return text.length > 200 ? text.substring(0, 200) + '...' : text
})

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
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }, { min: 2, max: 100, message: '标题长度 2-100 字', trigger: 'blur' }],
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
    const [postRes, catRes] = await Promise.all([getPostById(route.params.id), getCategories()])
    post.value = postRes.data
    categories.value = catRes || []

    // 从 post.categories 恢复已选分类
    if (post.value.categories && post.value.categories.length > 0) {
      selectedCategories.value = post.value.categories.map(c => ({ id: c.id, name: c.name, icon: c.icon }))
      form.value.categoryIds = selectedCategories.value.map(c => c.id)
    } else if (post.value.categoryId) {
      // 兼容旧数据
      const cat = categories.value.find(c => c.id === post.value.categoryId)
      if (cat) {
        selectedCategories.value = [{ id: cat.id, name: cat.name, icon: cat.icon }]
        form.value.categoryIds = [cat.id]
      }
    }

    form.value.title = post.value.title
    form.value.content = post.value.content
  } catch { router.push('/') }
  finally { loading.value = false }
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
    await updatePost(post.value.id, {
      title: form.value.title,
      content: form.value.content,
      summary: summary.value,
      categoryId: categoryIds.length > 0 ? categoryIds[0] : null,
      categoryIds: categoryIds,
    })
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
  border-radius: 8px;
  padding: 28px 32px;
  border: 1px solid #ebeef5;
}

h2 {
  font-size: 20px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f56c6c;
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
  .post-edit { padding: 20px 16px; }
  .cat-add-select { width: 160px; }
}
</style>
