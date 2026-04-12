<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>项目列表</span>
        <el-button type="primary" @click="showDialog = true">新建项目</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="projectName" label="项目名称" />
      <el-table-column prop="teamId" label="团队ID" width="100" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleGenKey(row.id)">生成Key</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:16px;justify-content:end" v-model:current-page="page" v-model:page-size="size"
      :total="total" layout="total, prev, pager, next" @current-change="loadData" />

    <el-dialog v-model="showDialog" title="新建项目" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="项目名称"><el-input v-model="form.projectName" /></el-form-item>
        <el-form-item label="团队ID"><el-input-number v-model="form.teamId" :min="1" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
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
import { getProjects, createProject, deleteProject, generateProxyKey } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showDialog = ref(false)
const form = reactive({ projectName: '', teamId: 1, description: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProjects({ current: page.value, size: size.value })
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleCreate = async () => {
  await createProject(form)
  ElMessage.success('创建成功')
  showDialog.value = false
  loadData()
}

const handleGenKey = async (id) => {
  const res = await generateProxyKey(id)
  ElMessageBox.alert(`代理Key: ${res.data.proxyKey}`, '生成成功', { confirmButtonText: '复制' })
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除?')
  await deleteProject(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
