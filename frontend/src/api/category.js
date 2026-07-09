import request from './request'

export function getCategories() {
  return request.get('/categories')
}

// 管理端
export function getAllCategories() {
  return request.get('/admin/categories')
}

export function createCategory(data) {
  return request.post('/admin/categories', data)
}

export function updateCategory(id, data) {
  return request.put(`/admin/categories/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/admin/categories/${id}`)
}
