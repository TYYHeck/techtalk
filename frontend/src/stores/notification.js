import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.data || 0
    } catch {
      unreadCount.value = 0
    }
  }

  function decrease() {
    if (unreadCount.value > 0) unreadCount.value--
  }

  return { unreadCount, fetchUnreadCount, decrease }
})
