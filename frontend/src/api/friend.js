import request from './request'

/** 获取好友列表（互关用户） */
export function getFriends() {
  return request.get('/friends')
}

/** 获取关注列表 */
export function getFollowing() {
  return request.get('/friends/following')
}

/** 获取粉丝列表 */
export function getFollowers() {
  return request.get('/friends/followers')
}

/** 关注用户 */
export function followUser(targetId) {
  return request.post(`/friends/follow/${targetId}`)
}

/** 取消关注 */
export function unfollowUser(targetId) {
  return request.post(`/friends/unfollow/${targetId}`)
}

/** 获取待处理的好友请求 */
export function getPendingRequests() {
  return request.get('/friends/requests')
}

/** 发送好友请求（兼容旧接口） */
export function sendFriendRequest(friendId) {
  return request.post(`/friends/follow/${friendId}`)
}

/** 接受好友请求 */
export function acceptFriendRequest(requestId) {
  return request.post(`/friends/accept/${requestId}`)
}

/** 拒绝好友请求 */
export function rejectFriendRequest(requestId) {
  return request.post(`/friends/reject/${requestId}`)
}

/** 移除好友/取消互关 */
export function removeFriend(friendId) {
  return request.delete(`/friends/${friendId}`)
}

/** 检查是否为互关好友 */
export function checkFriend(friendId) {
  return request.get(`/friends/check/${friendId}`)
}

/** 检查是否可以发私信 */
export function checkCanMessage(targetId) {
  return request.get(`/friends/can-message/${targetId}`)
}

