import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function refreshToken(refreshToken) {
  return request.post('/auth/refresh', { refreshToken })
}

export function logout() {
  return request.post('/auth/logout')
}

export function getCurrentUser() {
  return request.get('/user/me')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

/** 获取用户主页 */
export function getUserProfile(userId) {
  return request.get(`/user/${userId}`)
}

/** 修改密码 */
export function updatePassword(data) {
  return request.put('/user/password', data)
}

/** 修改邮箱 */
export function updateEmail(data) {
  return request.put('/user/email', data)
}

/** 发送邮箱验证码（purpose: register / resetPassword） */
export function sendEmailCode(data) {
  return request.post('/auth/send-code', data)
}
