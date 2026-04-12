<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>路由规则</span>
        <el-button type="primary" @click="showDialog = true">添加规则</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="ruleName" label="规则名称" />
      <el-table-column prop="conditionExpr" label="条件表达式" />
      <el-table-column prop="targetProvider" label="目标厂商" width="120" />
      <el-table-column prop="targetModel" label="目标模型" width="180" />
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column prop="fallbackModel" label="降级模型" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" title="添加路由规则" width="600">
      <el-form :model="form" label-width="100px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="条件表达式"><el-input v-model="form.conditionExpr" placeholder="#promptLength > 2000" /></el-form-item>
        <el-form-item label="目标厂商"><el-input v-model="form.targetProvider" /></el-form-item>
        <el-form-item label="目标模型"><el-input v-model="form.targetModel" /></el-form-item>
        <el-form-item label="优先级"><el-input-number v-model="form.priority" /></el-form-item>
        <el-form-item label="降级厂商"><el-input v-model="form.fallbackProvider" /></el-form-item>
        <el-form-item label="降级模型"><el-input v-model="form.fallbackModel" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRouteRules, createRouteRule, deleteRouteRule } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const showDialog = ref(false)
const form = reactive({ ruleName: '', conditionExpr: '', targetProvider: '', targetModel: '', priority: 0, fallbackProvider: '', fallbackModel: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRouteRules({ current: 1, size: 100 })
    list.value = res.data.records
  } finally { loading.value = false }
}

const handleCreate = async () => {
  await createRouteRule(form)
  ElMessage.success('创建成功')
  showDialog.value = false
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除?')
  await deleteRouteRule(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
