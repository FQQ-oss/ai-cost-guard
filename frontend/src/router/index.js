import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据看板' } },
      { path: 'projects', name: 'Projects', component: () => import('../views/Projects.vue'), meta: { title: '项目管理' } },
      { path: 'key-pool', name: 'KeyPool', component: () => import('../views/KeyPool.vue'), meta: { title: 'Key池管理' } },
      { path: 'pricing', name: 'Pricing', component: () => import('../views/Pricing.vue'), meta: { title: '模型定价' } },
      { path: 'route-rules', name: 'RouteRules', component: () => import('../views/RouteRules.vue'), meta: { title: '路由规则' } },
      { path: 'stats', name: 'Stats', component: () => import('../views/Stats.vue'), meta: { title: '用量统计' } },
      { path: 'alerts', name: 'Alerts', component: () => import('../views/Alerts.vue'), meta: { title: '告警管理' } },
      { path: 'audit', name: 'Audit', component: () => import('../views/Audit.vue'), meta: { title: '审计日志' } },
      { path: 'users', name: 'Users', component: () => import('../views/Users.vue'), meta: { title: '用户管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
