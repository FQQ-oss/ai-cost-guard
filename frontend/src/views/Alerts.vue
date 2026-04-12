<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>告警规则</span>
        <el-button type="primary" @click="showDialog = true">添加规则</el-button>
      </div>
    </template>
    <el-table :data="rules" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="ruleName" label="规则名称" />
      <el-table-column prop="alertType" label="类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.alertType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="projectId" label="项目ID" width="100" />
      <el-table-column prop="threshold" label="阈值" width="120" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider />
    <h4>告警历史</h4>
    <el-table :data="history" size="small">
      <el-table-column prop="ruleId" label="规则ID" width="80" />
      <el-table-column prop="alertContent" label="告警内容" />
      <el-table-column prop="notifyStatus" label="通知状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.notifyStatus === 1 ? 'success' : 'warning'">{{ row.notifyStatus === 1 ? '已通知' : '未通知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>

    <el-dialog v-model="showDialog" title="添加告警规则" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.alertType">
            <el-option label="预算告警" value="budget" />
            <el-option label="用量突增" value="spike" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目ID"><el-input-number v-model="form.projectId" :min="1" /></el-form-item>
        <el-form-item label="阈值"><el-input-number v-model="form.threshold" :precision="2" /></el-form-item>
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
import { getAlertRules, createAlertRule, deleteAlertRule, getAlertHistory } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const rules = ref([])
const history = ref([])
const loading = ref(false)
const showDialog = ref(false)
const form = reactive({ ruleName: '', alertType: 'budget', projectId: 1, threshold: 100 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAlertRules({ current: 1, size: 100 })
    rules.value = res.data.records
    const hRes = await getAlertHistory({ current: 1, size: 20 })
    history.value = hRes.data.records
  } finally { loading.value = false }
}

const handleCreate = async () => {
  await createAlertRule(form)
  ElMessage.success('创建成功')
  showDialog.value = false
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除?')
  await deleteAlertRule(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
