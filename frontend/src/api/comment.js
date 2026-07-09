import request from './request'

export function getCommentsByPost(postId) {
  return request.get(`/comments/post/${postId}`)
}

export function createComment(data) {
  return request.post('/comments', data)
}

export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}
