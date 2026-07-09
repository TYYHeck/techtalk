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

/** 发送邮箱验证码（purpose: register / resetPassword） */
export function sendEmailCode(data) {
  return request.post('/auth/send-code', data)
}
