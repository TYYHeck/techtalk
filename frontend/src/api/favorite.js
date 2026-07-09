import request from './request'

export function toggleFavorite(postId) {
  return request.post(`/favorites/toggle/${postId}`)
}

export function getFavorites(params) {
  return request.get('/favorites', { params })
}
