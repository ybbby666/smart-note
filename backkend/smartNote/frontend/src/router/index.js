import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/Home.vue')
        },
        {
          path: 'note/:id',
          name: 'NoteDetail',
          component: () => import('@/views/NoteDetail.vue')
        },
        {
          path: 'recycle-bin',
          name: 'RecycleBin',
          component: () => import('@/views/RecycleBin.vue')
        },
        {
          path: 'friends',
          name: 'Friends',
          component: () => import('@/views/Friends.vue')
        },
        {
          path: 'history',
          name: 'History',
          component: () => import('@/views/History.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPaths = ['/login', '/register']
  
  // 如果是公开路径,直接放行
  if (publicPaths.includes(to.path)) {
    // 如果已登录且访问登录页,重定向到首页
    if (token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
  } else {
    // 需要鉴权的路径
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
