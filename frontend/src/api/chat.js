import request from './request'

export function getConversations() {
  return request.get('/chat/conversations')
}

export function getMessages(otherUserId) {
  return request.get(`/chat/messages/${otherUserId}`)
}

export function getUnreadCount() {
  return request.get('/chat/unread')
}

export function markAsRead(otherUserId) {
  return request.post(`/chat/read/${otherUserId}`)
}

/** 检查是否可以向某用户发私信 */
export function checkCanMessage(targetId) {
  return request.get(`/chat/check/${targetId}`)
}

