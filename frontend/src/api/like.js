import request from './request'

export function toggleLike(targetType, targetId) {
  return request.post('/likes/toggle', null, {
    params: { targetType, targetId }
  })
}
