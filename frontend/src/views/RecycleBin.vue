<template>
  <div class="flex-1 flex flex-col h-full bg-gray-50">
    <!-- 顶部工具栏 -->
    <header class="h-16 border-b border-gray-200 flex items-center justify-between px-6 bg-white shadow-sm">
      <div class="flex items-center">
        <el-button 
          circle 
          icon="ArrowLeft" 
          size="small" 
          @click="router.back()" 
          class="mr-4"
        />
        <h1 class="text-xl font-bold text-gray-800 flex items-center">
          <Trash2 class="w-5 h-5 mr-2 text-gray-500" />
          回收站
        </h1>
      </div>
      <div class="flex items-center space-x-3">
        <el-button 
          v-if="recycleList.length > 0"
          type="danger" 
          size="small"
          @click="handleClearAll"
        >
          清空回收站
        </el-button>
      </div>
    </header>

    <!-- 回收站列表 -->
    <div class="flex-1 overflow-y-auto p-6">
      <div v-if="loading" class="flex items-center justify-center py-20">
        <el-icon class="is-loading text-4xl text-primary"><Loading /></el-icon>
      </div>

      <el-empty 
        v-else-if="recycleList.length === 0" 
        description="回收站为空"
        :image-size="120"
      >
        <template #description>
          <p class="text-gray-400">删除的笔记和文件夹会出现在这里</p>
        </template>
      </el-empty>

      <div v-else class="space-y-3">
        <div 
          v-for="item in recycleList" 
          :key="item.id"
          class="bg-white rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow border border-gray-100"
        >
          <div class="flex items-start justify-between">
            <div class="flex items-start flex-1">
              <!-- 图标 -->
              <div class="p-2 rounded-lg mr-3" :class="item.resourceType === 'note' ? 'bg-blue-100' : 'bg-yellow-100'">
                <component 
                  :is="item.resourceType === 'note' ? FileText : Folder" 
                  class="w-5 h-5" 
                  :class="item.resourceType === 'note' ? 'text-blue-600' : 'text-yellow-600'"
                />
              </div>
              
              <!-- 内容 -->
              <div class="flex-1">
                <h3 class="font-medium text-gray-800 mb-1">{{ item.resourceName }}</h3>
                <div class="flex items-center text-xs text-gray-400 space-x-3">
                  <span>{{ item.resourceType === 'note' ? '笔记' : '文件夹' }}</span>
                  <span>•</span>
                  <span>删除于 {{ formatTime(item.deleteTime) }}</span>
                  <span v-if="item.daysLeft" class="text-orange-500">• {{ Math.floor(item.daysLeft / 1440) }}天后自动清除</span>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="flex items-center space-x-2 ml-4">
              <el-button 
                size="small" 
                type="primary"
                @click="handleRestore(item)"
              >
                恢复
              </el-button>
              <el-button 
                size="small" 
                type="danger"
                text
                @click="handlePermanentDelete(item)"
              >
                彻底删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElIcon } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { Trash2, FileText, Folder } from 'lucide-vue-next'
import { 
  getRecycleBinList, 
  restoreFromRecycleBin, 
  permanentDelete, 
  clearRecycleBin 
} from '@/api/recycle'

const router = useRouter()
const loading = ref(false)
const recycleList = ref([])

// 获取回收站列表
const fetchRecycleList = async () => {
  loading.value = true
  try {
    const data = await getRecycleBinList()
    console.log('获取到的回收站数据:', data)
    recycleList.value = data || []
    if (recycleList.value.length > 0) {
      console.log('第一个项目:', recycleList.value[0])
    }
  } catch (error) {
    console.error('获取回收站列表失败', error)
    ElMessage.error('获取回收站列表失败')
  } finally {
    loading.value = false
  }
}

// 恢复
const handleRestore = async (item) => {
  console.log('准备恢复项目:', item)
  if (!item || !item.id) {
    ElMessage.error('无效的项目ID')
    return
  }
  try {
    console.log('调用恢复API, ID:', item.id)
    await restoreFromRecycleBin(item.id)
    ElMessage.success('恢复成功')
    await fetchRecycleList()
  } catch (error) {
    console.error('恢复失败', error)
    ElMessage.error('恢复失败')
  }
}

// 彻底删除
const handlePermanentDelete = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要彻底删除"${item.resourceName}"吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await permanentDelete(item.id)
    ElMessage.success('已彻底删除')
    await fetchRecycleList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 清空回收站
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空回收站吗？所有项目将被彻底删除，此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await clearRecycleBin()
    ElMessage.success('回收站已清空')
    await fetchRecycleList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败', error)
      ElMessage.error('清空失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 小于1小时
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }
  // 小于24小时
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  // 大于24小时
  const days = Math.floor(diff / 86400000)
  if (days < 7) {
    return `${days}天前`
  }
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchRecycleList()
})
</script>

<style scoped>
</style>
