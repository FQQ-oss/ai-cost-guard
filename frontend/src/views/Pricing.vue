<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>模型定价</span>
        <el-button type="primary" @click="showDialog = true">添加定价</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="provider" label="厂商" width="120" />
      <el-table-column prop="model" label="模型" />
      <el-table-column prop="inputPricePer1k" label="输入价格/1K" width="140" />
      <el-table-column prop="outputPricePer1k" label="输出价格/1K" width="140" />
      <el-table-column prop="currency" label="币种" width="80" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" title="添加模型定价" width="500">
      <el-form :model="form" label-width="120px">
        <el-form-item label="厂商"><el-input v-model="form.provider" /></el-form-item>
        <el-form-item label="模型名称"><el-input v-model="form.model" /></el-form-item>
        <el-form-item label="输入价格/1K"><el-input-number v-model="form.inputPricePer1k" :precision="6" :step="0.001" /></el-form-item>
        <el-form-item label="输出价格/1K"><el-input-number v-model="form.outputPricePer1k" :precision="6" :step="0.001" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPricing, addPricing } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

// Note: deletePricing is not in the api file, let's use a direct request
import request from '../api/request'
const deletePricingApi = id => request.delete(`/apikey/pricing/${id}`)

const list = ref([])
const loading = ref(false)
const showDialog = ref(false)
const form = reactive({ provider: '', model: '', inputPricePer1k: 0, outputPricePer1k: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPricing({ current: 1, size: 100 })
    list.value = res.data.records
  } finally { loading.value = false }
}

const handleAdd = async () => {
  await addPricing(form)
  ElMessage.success('添加成功')
  showDialog.value = false
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除?')
  await deletePricingApi(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
