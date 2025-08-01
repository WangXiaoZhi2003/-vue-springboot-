import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Sent from '@/views/Sent.vue'
import Compose from '@/views/Compose.vue'
import MailDetail from '@/views/MailDetail.vue'
import Settings from '@/views/Settings.vue'
import Starred from '@/views/Starred.vue'
import Drafts from '@/views/Drafts.vue' 
import Spam from '@/views/Spam.vue' 
import Digest from '@/views/Digest.vue'
import Trash from '@/views/Trash.vue'
const routes = [
  {
    path: '/',
    redirect: '/inbox'
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/inbox',
    name: 'inbox',
    component: () => import('@/views/Inbox.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/sent',
    name: 'sent',
    component: Sent,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'settings',
    component: Settings,
    meta: { requiresAuth: true }
  },
  {
    path: '/compose',
    name: 'compose',
    component: Compose,
    meta: { requiresAuth: true },
    props: route => ({ draftId: route.query.draftId }) 
  },
  {
    path: '/digest',
    name: 'digest',
    component: Digest,
    meta: { requiresAuth: true }
  },
  {
    path: '/spam',
    name: 'spam',
    component: Spam,
    meta: { requiresAuth: true }
  },
  {
    path: '/mail/:id',
    name: 'mail-detail',
    component: MailDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('@/views/Search.vue'),
    props: route => ({ query: route.query.q }),
    meta: { requiresAuth: true }
  },
  {
    path: '/starred',
    name: 'starred',
    component: Starred,
    meta: { requiresAuth: true }
  },

  {
    path: '/drafts',
    name: 'drafts',
    component: Drafts,
    meta: { requiresAuth: true }
  },
  {
    path: '/trash',
    name: 'trash',
    component: Trash,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫保持不变
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('auth_token')
  
  if (to.name === 'login' && isAuthenticated) {
    return next('/inbox')
  }
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next('/login')
  }
  
  next()
})

export default router
