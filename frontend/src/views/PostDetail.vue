<template>
  <div class="post-detail" v-loading="loading">
    <div v-if="post" class="detail-content">
      <!-- Breadcrumb -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span class="sep">/</span>
        <span class="current">{{ post.categoryName || '详情' }}</span>
      </div>

      <!-- Post Body -->
      <article class="main-card">
        <header class="post-header">
          <h1>{{ post.title }}</h1>
          <div class="header-meta">
            <div class="author-info">
              <el-avatar :size="44" :src="post.author?.avatar" />
              <div>
                <div class="author-name">{{ post.author?.username }}</div>
                <div class="post-time">
                  <el-icon><Clock /></el-icon> {{ formatTime(post.createdAt) }}
                  <span v-if="post.updatedAt !== post.createdAt" class="edited-tag">（已编辑）</span>
                </div>
              </div>
            </div>
            <div class="actions">
              <el-button @click="handleLike" :type="post.isLiked ? 'danger' : 'default'" :text="!post.isLiked" round>
                <el-icon><StarFilled v-if="post.isLiked" /><Star v-else /></el-icon>
                <span>{{ post.likeCount }}</span>
              </el-button>
              <el-button @click="handleFavorite" :type="post.isFavorited ? 'warning' : 'default'" :text="!post.isFavorited" round>
                <el-icon><StarFilled v-if="post.isFavorited" /><Collection v-else /></el-icon>
                收藏 {{ post.favoriteCount }}
              </el-button>
              <el-button v-if="isAuthor" @click="editPost" text round><el-icon><Edit /></el-icon>编辑</el-button>
              <el-button v-if="isAuthor" @click="handleDelete" text type="danger" round><el-icon><Delete /></el-icon>删除</el-button>
            </div>
          </div>
          <!-- Category & tags row -->
          <div class="post-label-row">
            <el-tag v-if="post.isPinned" type="danger" size="small" effect="dark" round>置顶</el-tag>
            <el-tag v-if="post.isFeatured" type="warning" size="small" effect="dark" round>精华</el-tag>
          </div>
        </header>
        <el-divider />
        <div class="post-body rich-content" v-html="post.content" />
        <div class="post-footer">
          <span><el-icon><View /></el-icon> {{ post.viewCount }} 次浏览</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }} 条评论</span>
        </div>
      </article>

      <!-- Comment Section -->
      <section class="comment-section">
        <h3><el-icon><ChatDotRound /></el-icon> 评论 ({{ post.commentCount }})</h3>

        <!-- Comment Input -->
        <div class="comment-form" v-if="authStore.isLoggedIn">
          <div class="comment-form-inner">
            <el-avatar :size="36" :src="authStore.user?.avatar" />
            <div class="form-right">
              <el-input v-model="newComment" type="textarea" :rows="3" placeholder="写下你的想法..." maxlength="2000" show-word-limit resize="none" />
              <div class="form-actions">
                <span class="form-hint">支持 Markdown 语法</span>
                <el-button type="primary" @click="submitComment" :loading="submitting" round>
                  发表评论
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="comment-login-hint">
          <router-link to="/login">登录</router-link> 后参与讨论
        </div>

        <!-- Comment List -->
        <div v-if="comments.length > 0" class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="38" :src="comment.user?.avatar" />
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author">{{ comment.user?.username }}</span>
                <span v-if="comment.replyToUser" class="reply-to">
                  回复 @{{ comment.replyToUser?.username }}
                </span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                <span v-if="comment.isLiked && comment.likeCount > 0" class="comment-popular">
                  <el-icon><StarFilled /></el-icon> {{ comment.likeCount }}
                </span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              <div class="comment-actions">
                <span @click="handleCommentLike(comment)" class="action-btn" :class="{ liked: comment.isLiked }">
                  <el-icon><StarFilled v-if="comment.isLiked" /><Star v-else /></el-icon>
                  {{ comment.likeCount || '点赞' }}
                </span>
                <span @click="showReplyBox(comment)" class="action-btn">
                  <el-icon><ChatLineSquare /></el-icon> 回复
                </span>
                <span v-if="canDeleteComment(comment)" @click="handleDeleteComment(comment.id)" class="action-btn danger">删除</span>
              </div>

              <!-- Reply Box -->
              <div v-if="replyingTo === comment.id" class="reply-form">
                <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="'回复 @' + comment.user?.username" maxlength="2000" resize="none" />
                <div class="reply-actions">
                  <el-button size="small" type="primary" @click="submitReply(comment)" :loading="submitting" round>回复</el-button>
                  <el-button size="small" @click="replyingTo = null" round>取消</el-button>
                </div>
              </div>

              <!-- Child Comments -->
              <div v-if="comment.children && comment.children.length > 0" class="child-comments">
                <div v-for="child in comment.children" :key="child.id" class="child-comment">
                  <el-avatar :size="30" :src="child.user?.avatar" />
                  <div class="child-body">
                    <div class="comment-header">
                      <span class="comment-author">{{ child.user?.username }}</span>
                      <span v-if="child.replyToUser" class="reply-to">回复 @{{ child.replyToUser?.username }}</span>
                      <span class="comment-time">{{ formatTime(child.createdAt) }}</span>
                    </div>
                    <p class="comment-text">{{ child.content }}</p>
                    <div class="comment-actions">
                      <span @click="handleCommentLike(child)" class="action-btn" :class="{ liked: child.isLiked }">
                        <el-icon><StarFilled v-if="child.isLiked" /><Star v-else /></el-icon>
                        {{ child.likeCount || '点赞' }}
                      </span>
                      <span v-if="canDeleteComment(child)" @click="handleDeleteComment(child.id)" class="action-btn danger">删除</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评论，来抢沙发！" :image-size="80" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostById, deletePost } from '@/api/post'
import { getCommentsByPost, createComment, deleteComment } from '@/api/comment'
import { toggleLike } from '@/api/like'
import { toggleFavorite } from '@/api/favorite'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const post = ref(null)
const comments = ref([])
const loading = ref(true)
const newComment = ref('')
const replyContent = ref('')
const replyingTo = ref(null)
const submitting = ref(false)

const isAuthor = computed(() => authStore.user?.id === post.value?.author?.id)

onMounted(async () => {
  try {
    const res = await getPostById(route.params.id)
    post.value = res.data
    await fetchComments()
  } catch {
    router.push('/')
  } finally {
    loading.value = false
  }
})

async function fetchComments() {
  try {
    const res = await getCommentsByPost(route.params.id)
    comments.value = res.data || []
  } catch { /* ignore */ }
}

async function handleLike() {
  if (!authStore.isLoggedIn) { router.push('/login'); return }
  const res = await toggleLike('POST', post.value.id)
  post.value.isLiked = res.data.liked
  post.value.likeCount += res.data.liked ? 1 : -1
}

async function handleFavorite() {
  if (!authStore.isLoggedIn) { router.push('/login'); return }
  const res = await toggleFavorite(post.value.id)
  post.value.isFavorited = res.data.favorited
  post.value.favoriteCount += res.data.favorited ? 1 : -1
  ElMessage.success(res.data.message)
}

async function handleCommentLike(comment) {
  if (!authStore.isLoggedIn) { router.push('/login'); return }
  const res = await toggleLike('COMMENT', comment.id)
  comment.isLiked = res.data.liked
  comment.likeCount += res.data.liked ? 1 : -1
}

async function submitComment() {
  if (!newComment.value.trim()) return
  submitting.value = true
  try {
    await createComment({ postId: post.value.id, content: newComment.value, parentId: 0 })
    newComment.value = ''
    ElMessage.success('评论成功')
    await fetchComments()
    post.value.commentCount++
  } catch { /* handled */ }
  finally { submitting.value = false }
}

function showReplyBox(comment) {
  if (!authStore.isLoggedIn) { router.push('/login'); return }
  replyingTo.value = replyingTo.value === comment.id ? null : comment.id
}

async function submitReply(parentComment) {
  if (!replyContent.value.trim()) return
  submitting.value = true
  try {
    await createComment({
      postId: post.value.id,
      content: replyContent.value,
      parentId: parentComment.id,
      replyToUserId: parentComment.user?.id,
    })
    replyContent.value = ''
    replyingTo.value = null
    ElMessage.success('回复成功')
    await fetchComments()
    post.value.commentCount++
  } catch { /* */ }
  finally { submitting.value = false }
}

function canDeleteComment(comment) {
  return authStore.user?.id === comment.user?.id ||
         authStore.user?.id === post.value?.author?.id ||
         authStore.isAdmin
}

async function handleDeleteComment(id) {
  try { await ElMessageBox.confirm('确定删除此评论？', '提示', { type: 'warning' }) }
  catch { return }
  await deleteComment(id)
  ElMessage.success('删除成功')
  await fetchComments()
  post.value.commentCount--
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除此帖子？此操作不可撤销！', '警告', {
      type: 'error', confirmButtonText: '删除', cancelButtonText: '取消'
    })
  } catch { return }
  await deletePost(post.value.id)
  ElMessage.success('删除成功')
  router.push('/')
}

function editPost() { router.push(`/edit/${post.value.id}`) }

function formatTime(time) {
  if (!time) return ''
  const d = dayjs(time)
  if (!d.isValid()) return ''
  const now = dayjs()
  const diff = now.diff(d, 'minute')
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff} 分钟前`
  if (diff < 1440) return `${Math.floor(diff/60)} 小时前`
  if (diff < 10080) return `${Math.floor(diff/1440)} 天前`
  return d.format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.post-detail { max-width: 820px; margin: 0 auto; }

/* ===== Breadcrumb ===== */
.breadcrumb {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}
.breadcrumb a { color: var(--text-secondary); }
.breadcrumb a:hover { color: var(--primary); }
.breadcrumb .sep { margin: 0 8px; }
.breadcrumb .current { color: var(--text-primary); font-weight: 500; }

/* ===== Main Card ===== */
.main-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 32px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
}

.post-header h1 {
  font-size: 28px;
  line-height: 1.4;
  margin-bottom: 18px;
  color: var(--text-primary);
  font-weight: 700;
}

.header-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-name { font-size: 15px; color: var(--text-primary); font-weight: 600; }
.post-time {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 3px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.edited-tag { color: var(--text-placeholder); }

.actions { display: flex; gap: 4px; flex-wrap: wrap; }

.post-label-row {
  display: flex;
  gap: 8px;
  margin-top: 14px;
}

/* ===== Post Body ===== */
.post-body {
  min-height: 240px;
}

.post-footer {
  display: flex;
  gap: 28px;
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid var(--border-lighter);
  font-size: 13px;
  color: var(--text-secondary);
}
.post-footer span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ===== Comment Section ===== */
.comment-section {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.comment-section h3 {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 22px;
  padding-bottom: 14px;
  border-bottom: 2px solid var(--primary);
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Comment Form */
.comment-form { margin-bottom: 24px; }
.comment-form-inner {
  display: flex;
  gap: 12px;
}
.form-right { flex: 1; }
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.form-hint { font-size: 12px; color: var(--text-placeholder); }

.comment-login-hint {
  text-align: center;
  padding: 24px;
  color: var(--text-secondary);
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  margin-bottom: 20px;
}

/* Comment List */
.comment-item {
  display: flex;
  gap: 14px;
  padding: 18px 0;
  border-bottom: 1px solid var(--border-lighter);
}
.comment-item:last-child { border-bottom: none; }

.comment-body { flex: 1; min-width: 0; }

.comment-header { margin-bottom: 6px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

.comment-author { font-weight: 600; font-size: 14px; color: var(--text-primary); }
.reply-to { color: var(--primary); font-size: 13px; }
.comment-time { font-size: 12px; color: var(--text-placeholder); }
.comment-popular {
  font-size: 12px;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 2px;
}

.comment-text {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-regular);
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 18px;
  margin-top: 8px;
}

.action-btn {
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}
.action-btn:hover { color: var(--primary); }
.action-btn.liked { color: #f56c6c; }
.action-btn.danger:hover { color: var(--danger); }

/* Reply Form */
.reply-form { margin-top: 12px; }
.reply-actions { margin-top: 8px; display: flex; gap: 8px; }

/* Child Comments */
.child-comments {
  margin-top: 14px;
  padding: 14px;
  background: #f7f8fa;
  border-radius: var(--radius-sm);
}
.child-comment {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}
.child-comment:last-child { border-bottom: none; padding-bottom: 0; }
.child-comment:first-child { padding-top: 0; }
.child-body { flex: 1; min-width: 0; }

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .main-card { padding: 20px; }
  .post-header h1 { font-size: 22px; }
  .header-meta { flex-direction: column; align-items: flex-start; }
  .actions { width: 100%; justify-content: flex-start; }
  .comment-section { padding: 20px; }
  .comment-form-inner { flex-direction: column; }
  .form-right .el-button { width: 100%; }
}
</style>
