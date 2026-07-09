<template>
  <div class="post-detail" v-loading="loading">
    <div v-if="post" class="detail-content">
      <!-- 帖子内容 -->
      <div class="main-card">
        <div class="post-header">
          <h1>{{ post.title }}</h1>
          <div class="header-meta">
            <div class="author-info">
              <el-avatar :size="40" :src="post.author?.avatar" />
              <div>
                <div class="author-name">{{ post.author?.username }}</div>
                <div class="post-time">{{ formatTime(post.createdAt) }}</div>
              </div>
            </div>
            <div class="actions">
              <el-button @click="handleLike" :type="post.isLiked ? 'danger' : 'default'" text>
                <el-icon><StarFilled v-if="post.isLiked" /><Star v-else /></el-icon>
                {{ post.likeCount }}
              </el-button>
              <el-button @click="handleFavorite" :type="post.isFavorited ? 'warning' : 'default'" text>
                <el-icon><StarFilled v-if="post.isFavorited" /><Collection v-else /></el-icon>
                收藏 {{ post.favoriteCount }}
              </el-button>
              <el-button v-if="isAuthor" @click="editPost" text><el-icon><Edit /></el-icon>编辑</el-button>
              <el-button v-if="isAuthor" @click="handleDelete" text type="danger"><el-icon><Delete /></el-icon>删除</el-button>
            </div>
          </div>
        </div>
        <el-divider />
        <div class="post-body rich-content" v-html="post.content" />
        <div class="post-footer">
          <span><el-icon><View /></el-icon> {{ post.viewCount }} 浏览</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }} 评论</span>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <h3>评论 ({{ post.commentCount }})</h3>

        <!-- 发表评论 -->
        <div class="comment-form" v-if="authStore.isLoggedIn">
          <el-input v-model="newComment" type="textarea" :rows="3" placeholder="写下你的评论..."
            maxlength="2000" show-word-limit />
          <el-button type="primary" @click="submitComment" :loading="submitting" style="margin-top:10px">
            发表评论
          </el-button>
        </div>
        <div v-else class="comment-login-hint">
          <router-link to="/login">登录</router-link> 后参与评论
        </div>

        <!-- 评论列表 -->
        <div v-if="comments.length > 0" class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="36" :src="comment.user?.avatar" />
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author">{{ comment.user?.username }}</span>
                <span v-if="comment.replyToUser" class="reply-to">
                  回复 @{{ comment.replyToUser?.username }}
                </span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              <div class="comment-actions">
                <span @click="handleCommentLike(comment)" class="action-btn">
                  <el-icon><StarFilled v-if="comment.isLiked" style="color:#f56c6c" /><Star v-else /></el-icon>
                  {{ comment.likeCount || '' }}
                </span>
                <span @click="showReplyBox(comment)" class="action-btn">
                  <el-icon><ChatLineSquare /></el-icon> 回复
                </span>
                <span v-if="canDeleteComment(comment)" @click="handleDeleteComment(comment.id)" class="action-btn delete-btn">
                  删除
                </span>
              </div>

              <!-- 回复输入框 -->
              <div v-if="replyingTo === comment.id" class="reply-form">
                <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="'回复 @' + comment.user?.username"
                  maxlength="2000" show-word-limit />
                <div style="margin-top:8px">
                  <el-button size="small" type="primary" @click="submitReply(comment)" :loading="submitting">回复</el-button>
                  <el-button size="small" @click="replyingTo = null">取消</el-button>
                </div>
              </div>

              <!-- 子评论 -->
              <div v-if="comment.children && comment.children.length > 0" class="child-comments">
                <div v-for="child in comment.children" :key="child.id" class="child-comment">
                  <el-avatar :size="28" :src="child.user?.avatar" />
                  <div class="child-body">
                    <div class="comment-header">
                      <span class="comment-author">{{ child.user?.username }}</span>
                      <span v-if="child.replyToUser" class="reply-to">
                        回复 @{{ child.replyToUser?.username }}
                      </span>
                      <span class="comment-time">{{ formatTime(child.createdAt) }}</span>
                    </div>
                    <p class="comment-text">{{ child.content }}</p>
                    <div class="comment-actions">
                      <span @click="handleCommentLike(child)" class="action-btn">
                        <el-icon><StarFilled v-if="child.isLiked" style="color:#f56c6c" /><Star v-else /></el-icon>
                        {{ child.likeCount || '' }}
                      </span>
                      <span v-if="canDeleteComment(child)" @click="handleDeleteComment(child.id)" class="action-btn delete-btn">删除</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评论，来抢沙发吧！" />
      </div>
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

const isAuthor = computed(() =>
  authStore.user?.id === post.value?.author?.id
)

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
  } catch { /* handled by interceptor */ }
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
  } catch { /*  */ }
  finally { submitting.value = false }
}

function canDeleteComment(comment) {
  return authStore.user?.id === comment.user?.id ||
         authStore.user?.id === post.value?.author?.id ||
         authStore.isAdmin
}

async function handleDeleteComment(id) {
  try {
    await ElMessageBox.confirm('确定删除此评论？', '提示', { type: 'warning' })
  } catch { return }
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

function editPost() {
  router.push(`/edit/${post.value.id}`)
}

function formatTime(time) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.post-detail { max-width: 800px; margin: 0 auto; }

.main-card {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
}

.post-header h1 {
  font-size: 26px;
  line-height: 1.4;
  margin-bottom: 16px;
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
  gap: 10px;
}

.author-name { font-size: 15px; color: #303133; font-weight: 500; }
.post-time { font-size: 12px; color: #909399; margin-top: 2px; }

.actions { display: flex; gap: 4px; }

.post-body {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  min-height: 200px;
}

.post-footer {
  display: flex;
  gap: 24px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  font-size: 13px;
  color: #909399;
}

.comment-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
}

.comment-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
}

.comment-form { margin-bottom: 24px; }

.comment-login-hint {
  text-align: center;
  padding: 20px;
  color: #909399;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f2f5;
}

.comment-body { flex: 1; }

.comment-header { margin-bottom: 6px; }

.comment-author { font-weight: 500; font-size: 14px; color: #303133; }
.reply-to { color: #409eff; font-size: 13px; margin-left: 8px; }
.comment-time { font-size: 12px; color: #c0c4cc; margin-left: 12px; }

.comment-text { font-size: 15px; line-height: 1.6; color: #303133; }

.comment-actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.action-btn {
  font-size: 13px;
  color: #909399;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 3px;
  transition: color 0.2s;
}

.action-btn:hover { color: #409eff; }
.delete-btn:hover { color: #f56c6c; }

.reply-form { margin-top: 12px; }

.child-comments {
  margin-top: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.child-comment {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
}

.child-comment:last-child { border-bottom: none; }
.child-body { flex: 1; }
</style>
