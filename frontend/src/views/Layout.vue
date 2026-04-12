<template>
  <el-container style="height:100vh">
    <el-aside width="220px" style="background:#001529">
      <div style="height:60px;display:flex;align-items:center;justify-content:center">
        <h3 style="color:#fff;margin:0">AI Cost Guard</h3>
      </div>
      <el-menu :default-active="route.path" router background-color="#001529" text-color="#ffffffa6" active-text-color="#fff">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>数据看板</span></el-menu-item>
        <el-menu-item index="/projects"><el-icon><Folder /></el-icon><span>项目管理</span></el-menu-item>
        <el-menu-item index="/key-pool"><el-icon><Key /></el-icon><span>Key池管理</span></el-menu-item>
        <el-menu-item index="/pricing"><el-icon><PriceTag /></el-icon><span>模型定价</span></el-menu-item>
        <el-menu-item index="/route-rules"><el-icon><Guide /></el-icon><span>路由规则</span></el-menu-item>
        <el-menu-item index="/stats"><el-icon><TrendCharts /></el-icon><span>用量统计</span></el-menu-item>
        <el-menu-item index="/alerts"><el-icon><Bell /></el-icon><span>告警管理</span></el-menu-item>
        <el-menu-item index="/audit"><el-icon><Document /></el-icon><span>审计日志</span></el-menu-item>
        <el-menu-item index="/users"><el-icon><UserFilled /></el-icon><span>用户管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid #e8e8e8">
        <h4 style="margin:0">{{ route.meta.title }}</h4>
        <div>
          <span style="margin-right:16px">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
          <el-button text @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main style="background:#f0f2f5;padding:20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { logout } from '../api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  try { await logout() } catch {}
  userStore.clearLogin()
  router.push('/login')
}
</script>
