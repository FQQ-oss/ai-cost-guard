<template>
  <div>
    <el-card style="margin-bottom:16px">
      <el-form inline>
        <el-form-item label="项目ID"><el-input-number v-model="query.projectId" :min="0" clearable /></el-form-item>
        <el-form-item label="起始日期"><el-date-picker v-model="query.startDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="query.endDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="总请求数" :value="summary.totalRequests || 0" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="总Token" :value="summary.totalTokens || 0" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="总花费($)" :value="summary.totalCost || 0" :precision="4" /></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="缓存命中率" :value="summary.cacheHitRate || 0" suffix="%" /></el-card></el-col>
    </el-row>

    <el-card>
      <template #header>用量趋势</template>
      <div ref="chartRef" style="height:400px"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getStatsSummary, getStatsTrend } from '../api'

const query = reactive({ projectId: null, startDate: '', endDate: '' })
const summary = ref({})
const chartRef = ref()
let chart = null

const loadData = async () => {
  const params = { ...query }
  if (!params.projectId) delete params.projectId

  try {
    const res = await getStatsSummary(params)
    summary.value = res.data || {}
  } catch {}

  try {
    const res = await getStatsTrend(params)
    const data = res.data || []
    if (!chart) chart = echarts.init(chartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['请求数', 'Token消耗', '花费($)'] },
      xAxis: { type: 'category', data: data.map(d => d.date) },
      yAxis: [{ type: 'value', name: '数量' }, { type: 'value', name: '花费($)' }],
      series: [
        { name: '请求数', type: 'bar', data: data.map(d => d.requests) },
        { name: 'Token消耗', type: 'bar', data: data.map(d => (d.promptTokens || 0) + (d.completionTokens || 0)) },
        { name: '花费($)', type: 'line', yAxisIndex: 1, data: data.map(d => d.cost) }
      ]
    })
  } catch {}
}

onMounted(() => {
  const d = new Date()
  query.endDate = d.toISOString().slice(0, 10)
  d.setDate(d.getDate() - 30)
  query.startDate = d.toISOString().slice(0, 10)
  loadData()
})
</script>
