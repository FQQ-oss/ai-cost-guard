<template>
  <el-card>
    <template #header>审计日志</template>
    <el-form inline style="margin-bottom:16px">
      <el-form-item label="操作类型"><el-input v-model="query.action" placeholder="如: 创建项目" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="操作人" width="120" />
      <el-table-column prop="action" label="操作" width="120" />
      <el-table-column prop="resource" label="资源" width="120" />
      <el-table-column prop="detail" label="详情" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>
    <el-pagination style="margin-top:16px;justify-content:end" v-model:current-page="page" v-model:page-size="size"
      :total="total" layout="total, prev, pager, next" @current-change="loadData" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAuditLogs } from '../api'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const query = reactive({ action: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAuditLogs({ current: page.value, size: size.value, ...query })
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(loadData)
</script>
