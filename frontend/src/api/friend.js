import request from './request'

export function getFriends() {
  return request.get('/friends')
}

export function getPendingRequests() {
  return request.get('/friends/requests')
}

export function sendFriendRequest(friendId) {
  return request.post(`/friends/request/${friendId}`)
}

export function acceptFriendRequest(requestId) {
  return request.post(`/friends/accept/${requestId}`)
}

export function rejectFriendRequest(requestId) {
  return request.post(`/friends/reject/${requestId}`)
}

export function removeFriend(friendId) {
  return request.delete(`/friends/${friendId}`)
}

export function checkFriend(friendId) {
  return request.get(`/friends/check/${friendId}`)
}
