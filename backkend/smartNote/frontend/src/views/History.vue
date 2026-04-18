<template>
  <div class="flex-1 flex flex-col h-full bg-white overflow-hidden">
    <!-- 头部工具栏 -->
    <header class="h-16 border-b border-gray-100 flex items-center justify-between px-8 bg-white/80 backdrop-blur-md sticky top-0 z-10 shadow-sm">
      <div class="flex items-center">
        <h2 class="text-xl font-bold text-gray-800">浏览历史</h2>
        <span class="ml-3 px-2 py-0.5 bg-gray-100 text-gray-500 rounded-full text-xs font-medium">
          {{ historyNotes.length }} 篇
        </span>
      </div>
      <div class="flex items-center space-x-2 text-sm text-gray-500">
        <Clock class="w-4 h-4" />
        <span>按最近浏览时间排序</span>
      </div>
    </header>

    <!-- 历史记录列表 -->
    <div class="flex-1 overflow-y-auto p-8 custom-scrollbar bg-gray-50/30">
      <div class="grid grid-cols-1 max-w-4xl mx-auto gap-4">
        <el-card 
          v-for="note in historyNotes" 
          :key="note.id" 
          shadow="hover" 
          class="cursor-pointer border-none shadow-sm hover:shadow-md transition-all duration-300 transform hover:-translate-y-1 p-4"
          @click="openNote(note.id)"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1 min-w-0">
              <h3 class="text-lg font-semibold text-gray-900 line-clamp-1 mb-2 group-hover:text-primary transition-colors">
                {{ note.title || '无标题' }}
              </h3>
              
              <p class="text-gray-500 text-sm mb-3 line-clamp-2 leading-relaxed">
                {{ note.content?.substring(0, 150).replace(/[#*`]/g, '') || '暂无内容' }}
              </p>
              
              <div class="flex items-center justify-between">
                <div class="flex items-center space-x-2 flex-1 min-w-0">
                  <!-- 显示创建者信息（如果不是当前用户） -->
                  <div v-if="note.creatorId && note.creatorId !== userStore.userInfo.id" class="flex items-center text-xs text-gray-500 mr-2">
                    <User class="w-3 h-3 mr-1" />
                    <span class="truncate">{{ note.creatorName || '未知用户' }}</span>
                  </div>
                  
                  <el-tag 
                    v-for="tag in note.tags" 
                    :key="tag" 
                    size="small" 
                    effect="plain" 
                    class="rounded-full border-gray-200 text-gray-500"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <span class="text-xs text-blue-500 flex items-center ml-2" title="浏览时间">
                  <Clock class="w-3 h-3 mr-1" />
                  {{ formatViewTime(note.viewTime) }}
                </span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 空状态 -->
        <div v-if="historyNotes.length === 0" class="col-span-full flex flex-col items-center justify-center py-20 bg-white rounded-2xl shadow-sm border border-dashed border-gray-200">
          <el-empty description="还没有浏览记录哦，快去看看吧！" :image-size="200" />
          <el-button type="primary" size="large" icon="FileText" class="mt-4" @click="router.push('/')">浏览笔记</el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="flex justify-center mt-8 pb-8">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, User, FileText } from 'lucide-vue-next'
import dayjs from 'dayjs'
import { getNoteHistory } from '@/api/note'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const historyNotes = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchHistory = async () => {
  try {
    console.log('开始获取浏览历史...')
    const data = await getNoteHistory({ 
      pageNum: currentPage.value, 
      pageSize: pageSize.value 
    })
    console.log('获取到的历史记录:', data)
    historyNotes.value = data.records || []
    total.value = data.total || 0
    console.log('历史记录数量:', historyNotes.value.length, '总数:', total.value)
  } catch (error) {
    console.error('获取历史记录失败', error)
    ElMessage.error('获取浏览历史失败')
  }
}

const openNote = (id) => {
  router.push(`/note/${id}`)
}

const formatDate = (date) => {
  return dayjs(date).format('MM-DD HH:mm')
}

const formatViewTime = (date) => {
  if (!date) return ''
  const now = dayjs()
  const viewTime = dayjs(date)
  const diffMinutes = now.diff(viewTime, 'minute')
  
  // 根据时间差显示不同的格式
  if (diffMinutes < 1) {
    return '刚刚'
  } else if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`
  } else if (diffMinutes < 1440) { // 24小时
    return `${Math.floor(diffMinutes / 60)}小时前`
  } else if (diffMinutes < 10080) { // 7天
    return `${Math.floor(diffMinutes / 1440)}天前`
  } else {
    return viewTime.format('MM-DD HH:mm')
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchHistory()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchHistory()
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}
</style>
