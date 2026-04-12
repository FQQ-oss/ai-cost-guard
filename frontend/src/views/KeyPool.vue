<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>LLM Key池</span>
        <el-button type="primary" @click="showDialog = true">添加Key</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="provider" label="厂商" width="120" />
      <el-table-column prop="keyName" label="备注" />
      <el-table-column prop="apiKeyEncrypted" label="Key" />
      <el-table-column prop="rpmLimit" label="RPM" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="val => handleToggle(row.id, val ? 1 : 0)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" title="添加API Key" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="厂商">
          <el-select v-model="form.provider">
            <el-option label="OpenAI" value="openai" />
            <el-option label="Claude" value="claude" />
            <el-option label="DeepSeek" value="deepseek" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.keyName" /></el-form-item>
        <el-form-item label="API Key"><el-input v-model="form.apiKey" type="password" show-password /></el-form-item>
        <el-form-item label="Base URL"><el-input v-model="form.baseUrl" placeholder="留空使用默认" /></el-form-item>
        <el-form-item label="RPM"><el-input-number v-model="form.rpmLimit" :min="1" /></el-form-item>
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
import { getKeyPool, addKey, deleteKey, toggleKeyStatus } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const showDialog = ref(false)
const form = reactive({ provider: 'openai', keyName: '', apiKey: '', baseUrl: '', rpmLimit: 60 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getKeyPool({ current: 1, size: 100 })
    list.value = res.data.records
  } finally { loading.value = false }
}

const handleAdd = async () => {
  await addKey(form)
  ElMessage.success('添加成功')
  showDialog.value = false
  loadData()
}

const handleToggle = async (id, status) => {
  await toggleKeyStatus(id, status)
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除?')
  await deleteKey(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
