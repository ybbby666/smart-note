import request from '@/utils/request'

/**
 * 用户登录
 */
export function login(data) {
  return request.post('/users/login', data)
}

/**
 * 邮箱验证码登录
 */
export function emailLogin(data) {
  return request.post('/users/login/email', data)
}

/**
 * 用户注册
 */
export function register(data) {
  return request.post('/users/register', data)
}

/**
 * 用户登出
 */
export function logout() {
  return request.post('/users/logout')
}

/**
 * 刷新Token
 */
export function refreshToken(refreshToken) {
  return request.post('/users/token/refresh', { refreshToken })
}

/**
 * 获取当前用户资料
 */
export function getMyProfile() {
  return request.get('/users/profile')
}

/**
 * 获取指定用户资料
 */
export function getUserProfile(userId) {
  return request.get(`/users/${userId}/profile`)
}

/**
 * 更新用户资料
 */
export function updateProfile(data) {
  return request.put('/users/profile', data)
}

/**
 * 修改密码
 */
export function updatePassword(data) {
  return request.put('/users/password', data)
}

/**
 * 发送验证码
 */
export function sendVerificationCode(data) {
  return request.post('/users/verification-code', data)
}

/**
 * 重置密码
 */
export function resetPassword(data) {
  return request.post('/users/password/reset', data)
}
