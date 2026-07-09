import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'post/:id', name: 'PostDetail', component: () => import('@/views/PostDetail.vue') },
      { path: 'create', name: 'PostCreate', component: () => import('@/views/PostCreate.vue'), meta: { requireAuth: true } },
      { path: 'edit/:id', name: 'PostEdit', component: () => import('@/views/PostEdit.vue'), meta: { requireAuth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { requireAuth: true } },
      { path: 'notifications', name: 'Notifications', component: () => import('@/views/Notifications.vue'), meta: { requireAuth: true } },
      { path: 'favorites', name: 'Favorites', component: () => import('@/views/Favorites.vue'), meta: { requireAuth: true } },
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'users', name: 'UserManage', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'posts', name: 'PostManage', component: () => import('@/views/admin/PostManage.vue') },
      { path: 'comments', name: 'CommentManage', component: () => import('@/views/admin/CommentManage.vue') },
      { path: 'categories', name: 'CategoryManage', component: () => import('@/views/admin/CategoryManage.vue') },
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 需要登录
  if (to.meta.requireAuth && !authStore.isLoggedIn) {
    return next('/login')
  }

  // 需要管理员
  if (to.meta.requireAdmin && !authStore.isAdmin) {
    return next('/')
  }

  // 已登录用户不能访问登录/注册页
  if (to.meta.guest && authStore.isLoggedIn) {
    return next('/')
  }

  next()
})

export default router
