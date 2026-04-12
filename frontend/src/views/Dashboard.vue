<template>
  <div>
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日请求数" :value="summary.totalRequests || 0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日Token消耗" :value="summary.totalTokens || 0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日花费($)" :value="summary.totalCost || 0" :precision="4" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="缓存命中率" :value="summary.cacheHitRate || 0" suffix="%" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图 -->
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header>请求量趋势（近7天）</template>
          <div ref="trendChartRef" style="height:350px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>模型用量分布</template>
          <div ref="modelChartRef" style="height:350px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getStatsSummary, getStatsTrend, getStatsByModel } from '../api'

const summary = ref({})
const trendChartRef = ref()
const modelChartRef = ref()

const today = () => new Date().toISOString().slice(0, 10)
const daysAgo = n => {
  const d = new Date()
  d.setDate(d.getDate() - n)
  return d.toISOString().slice(0, 10)
}

onMounted(async () => {
  // 加载汇总
  try {
    const res = await getStatsSummary({ startDate: today(), endDate: today() })
    summary.value = res.data || {}
  } catch {}

  // 趋势图
  try {
    const res = await getStatsTrend({ startDate: daysAgo(7), endDate: today() })
    const data = res.data || []
    const chart = echarts.init(trendChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(d => d.date) },
      yAxis: [
        { type: 'value', name: '请求数' },
        { type: 'value', name: '花费($)' }
      ],
      series: [
        { name: '请求数', type: 'bar', data: data.map(d => d.requests), itemStyle: { color: '#409EFF' } },
        { name: '花费($)', type: 'line', yAxisIndex: 1, data: data.map(d => d.cost), itemStyle: { color: '#E6A23C' } }
      ]
    })
  } catch {}

  // 模型分布
  try {
    const res = await getStatsByModel({ startDate: daysAgo(7), endDate: today() })
    const data = res.data || []
    const chart = echarts.init(modelChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: data.map(d => ({ name: d.model, value: d.totalRequests })),
        emphasis: { itemStyle: { shadowBlur: 10 } }
      }]
    })
  } catch {}
})
</script>
