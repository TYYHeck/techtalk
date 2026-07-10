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
