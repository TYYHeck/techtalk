import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status, data, config } = error.response
      switch (status) {
        case 401: {
          const authStore = useAuthStore()
          // 登出接口本身的401跳过，避免无限循环
          if (config.url && config.url.includes('/auth/logout')) break
          // 轮询类请求的401静默处理，不弹提示不跳转
          if (config.url && config.url.includes('/notifications/unread-count')) {
            if (authStore.token) {
              authStore.logout()
            }
            break
          }
          // 未登录时401是正常的，直接跳转登录页，不调用logout
          if (!authStore.token) {
            router.push('/login')
            break
          }
          // 已登录但token过期，才需要清空登录状态
          ElMessage.error('登录已过期，请重新登录')
          authStore.logout()
          router.push('/login')
          break
        }
        case 403:
          ElMessage.error(data?.message || '权限不足')
          break
        case 429:
          ElMessage.error('操作过于频繁，请稍后再试')
          break
        default:
          ElMessage.error(data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
