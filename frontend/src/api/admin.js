import request from './request'

// 仪表盘
export function getDashboard() {
  return request.get('/admin/dashboard')
}

// 用户管理
export function getAdminUsers(params) {
  return request.get('/admin/users', { params })
}

export function updateUserStatus(id, data) {
  return request.put(`/admin/users/${id}/status`, data)
}

export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}

// 帖子管理
export function getAdminPosts(params) {
  return request.get('/admin/posts', { params })
}

export function updatePostStatus(id, data) {
  return request.put(`/admin/posts/${id}/status`, data)
}

export function togglePostPin(id) {
  return request.put(`/admin/posts/${id}/pin`)
}

export function togglePostFeature(id) {
  return request.put(`/admin/posts/${id}/feature`)
}

export function adminDeletePost(id) {
  return request.delete(`/admin/posts/${id}`)
}

// 评论管理
export function getAdminComments(params) {
  return request.get('/admin/comments', { params })
}

export function adminDeleteComment(id) {
  return request.delete(`/admin/comments/${id}`)
}
