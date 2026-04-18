<template>
  <div class="flex-1 flex flex-col h-full bg-white overflow-hidden">
    <!-- 头部工具栏 -->
    <header class="h-16 border-b border-gray-100 flex items-center justify-between px-8 bg-white/80 backdrop-blur-md sticky top-0 z-10 shadow-sm">
      <div class="flex items-center">
        <h2 class="text-xl font-bold text-gray-800">{{ pageTitle }}</h2>
        <span class="ml-3 px-2 py-0.5 bg-gray-100 text-gray-500 rounded-full text-xs font-medium">
          {{ notes.length }} 篇
        </span>
      </div>
      <div class="flex items-center space-x-3">
        <el-button-group>
          <el-button :icon="List" size="small" :type="viewMode === 'list' ? 'primary' : ''" @click="viewMode = 'list'" />
          <el-button :icon="LayoutGrid" size="small" :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'" />
        </el-button-group>
        <el-dropdown trigger="click">
          <el-button :icon="SortAsc" size="small">排序方式</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>最后修改</el-dropdown-item>
              <el-dropdown-item>创建日期</el-dropdown-item>
              <el-dropdown-item>标题名称</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 笔记列表 -->
    <div class="flex-1 overflow-y-auto p-8 custom-scrollbar bg-gray-50/30">
      <div 
        :class="[
          'grid gap-6',
          viewMode === 'grid' ? 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4' : 'grid-cols-1 max-w-4xl mx-auto'
        ]"
      >
        <el-card 
          v-for="note in notes" 
          :key="note.id" 
          shadow="hover" 
          :class="[
            'note-card cursor-pointer border-none shadow-sm hover:shadow-md transition-all duration-300 transform hover:-translate-y-1',
            viewMode === 'list' ? 'flex flex-row items-center p-4' : 'flex flex-col'
          ]"
          @click="openNote(note.id)"
        >
          <div :class="[viewMode === 'grid' ? 'p-6' : 'flex-1']">
            <div class="flex items-start justify-between mb-3">
              <h3 class="text-lg font-semibold text-gray-900 line-clamp-1 group-hover:text-primary transition-colors">
                {{ note.title || '无标题' }}
              </h3>
              <el-dropdown @click.stop trigger="click">
                <el-button 
                  circle 
                  size="small" 
                  :icon="MoreHorizontal"
                  class="text-gray-400 hover:text-gray-600 hover:bg-gray-100"
                  @click.stop
                />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :icon="Star">收藏</el-dropdown-item>
                    <el-dropdown-item :icon="Folder" @click="handleOpenMoveDialog(note)">移动到</el-dropdown-item>
                    <el-dropdown-item :icon="Share">分享</el-dropdown-item>
                    <el-dropdown-item divided :icon="Trash2" class="text-red-500" @click="handleDelete(note.id)">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            
            <p class="text-gray-500 text-sm mb-4 line-clamp-3 leading-relaxed">
              {{ note.content?.substring(0, 100).replace(/[#*`]/g, '') || '暂无内容' }}
            </p>

            <div class="flex items-center justify-between mt-auto pt-4 border-t border-gray-50">
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
                  class="rounded-full border-gray-200 text-gray-500 cursor-pointer hover:bg-blue-50 hover:text-blue-600 hover:border-blue-300 transition-colors"
                  @click.stop="handleFilterByTag(tag)"
                  :title="`点击筛选标签: ${tag}`"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <span class="text-xs text-gray-400 flex items-center ml-2">
                <Clock class="w-3 h-3 mr-1" />
                {{ formatDate(note.updateTime) }}
              </span>
            </div>
          </div>
        </el-card>

        <!-- 空状态 -->
        <div v-if="notes.length === 0" class="col-span-full flex flex-col items-center justify-center py-20 bg-white rounded-2xl shadow-sm border border-dashed border-gray-200">
          <el-empty description="暂时没有笔记哦，快去创建一个吧！" :image-size="200" />
          <el-button type="primary" size="large" icon="Plus" class="mt-4" @click="handleNewNote">创建第一篇笔记</el-button>
        </div>
      </div>
    </div>

    <!-- 移动到文件夹对话框 -->
    <el-dialog v-model="showMoveDialog" title="移动到文件夹" width="400px">
      <el-radio-group v-model="selectedFolderId" class="flex flex-col space-y-3">
        <el-radio :value="null" border class="w-full p-4">
          <span class="font-medium">无文件夹</span>
        </el-radio>
        <el-radio 
          v-for="folder in folders" 
          :key="folder.id" 
          :value="folder.id"
          border
          class="w-full p-4"
        >
          <span class="font-medium">{{ folder.name }}</span>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="showMoveDialog = false">取消</el-button>
        <el-button type="primary" @click="handleMoveToFolder">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  List, 
  LayoutGrid, 
  SortAsc, 
  Clock,
  Plus,
  Star,
  Folder,
  Share,
  Trash2,
  MoreHorizontal,
  User
} from 'lucide-vue-next'
import dayjs from 'dayjs'
import { getNoteList, deleteNote, updateNote } from '@/api/note'
import { getFolderTree } from '@/api/folder'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const viewMode = ref('grid')
const notes = ref([])
const folders = ref([])
const showMoveDialog = ref(false)
const selectedFolderId = ref(null)
const currentNote = ref(null) // 当前要移动的笔记

// 计算页面标题
const pageTitle = computed(() => {
  const folderId = route.query.folderId
  const keyword = route.query.keyword
  const tag = route.query.tag
  
  // 如果有搜索关键词，显示搜索结果
  if (keyword) {
    return `搜索: ${keyword}`
  }
  
  // 如果有标签，显示标签筛选
  if (tag) {
    return `标签: #${tag}`
  }
  
  if (folderId === undefined || folderId === null) {
    // 没有 folderId 参数，显示所有可看的笔记
    return '全部笔记'
  } else if (folderId === '0' || folderId === 0) {
    // folderId=0，显示未归类的笔记
    return '未分类笔记'
  } else {
    // 显示特定文件夹的名称
    const folder = folders.value.find(f => f.id === Number(folderId))
    return folder ? folder.name : '文件夹'
  }
})

const fetchNotes = async () => {
  try {
    console.log('开始获取笔记列表...')
    const params = { pageNum: 1, pageSize: 100 }
    
    // 如果有搜索关键词，添加到查询条件
    if (route.query.keyword) {
      params.keyword = route.query.keyword
      console.log('按关键词搜索:', route.query.keyword)
    }
    
    // 如果有标签参数，添加到查询条件
    if (route.query.tag) {
      params.tag = route.query.tag
      console.log('按标签搜索:', route.query.tag)
    }
    
    // 如果有文件夹ID参数，添加到查询条件
    if (route.query.folderId !== undefined) {
      params.folderId = route.query.folderId
      console.log('按文件夹筛选:', route.query.folderId, '类型:', typeof route.query.folderId)
    } else {
      console.log('未指定文件夹，显示所有笔记')
    }
    
    const data = await getNoteList(params)
    console.log('获取到的笔记数据:', data)
    notes.value = data.records || data
    console.log('笔记数量:', notes.value.length, '笔记列表:', notes.value)
  } catch (error) {
    console.error('获取笔记失败', error)
    ElMessage.error('获取笔记列表失败')
  }
}

const openNote = (id) => {
  router.push(`/note/${id}`)
}

// 按标签筛选
const handleFilterByTag = (tag) => {
  console.log('点击标签筛选:', tag)
  router.push({ path: '/', query: { tag } })
}

const handleNewNote = () => {
  console.log('点击创建笔记按钮')
  router.push('/note/new')
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这篇笔记吗？删除后将移至回收站',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    console.log('开始删除笔记:', id)
    await deleteNote(id)
    console.log('删除成功,刷新列表')
    ElMessage.success('已移至回收站')
    // 强制刷新列表
    await fetchNotes()
    console.log('列表已刷新,当前笔记数量:', notes.value.length)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

const formatDate = (date) => {
  return dayjs(date).format('MM-DD HH:mm')
}

// 获取文件夹列表
const fetchFolders = async () => {
  try {
    console.log('开始获取文件夹列表...')
    const data = await getFolderTree()
    console.log('获取到的文件夹数据:', data)
    folders.value = data || []
  } catch (error) {
    console.error('获取文件夹失败', error)
  }
}

// 打开移动对话框
const handleOpenMoveDialog = (note) => {
  console.log('点击移动到按钮, note:', note)
  currentNote.value = note
  selectedFolderId.value = note.folderId || null
  showMoveDialog.value = true
  console.log('对话框状态:', showMoveDialog.value)
}

// 移动到文件夹
const handleMoveToFolder = async () => {
  if (!currentNote.value) {
    ElMessage.warning('请选择要移动的笔记')
    return
  }
  try {
    console.log('移动笔记到文件夹:', currentNote.value.id, 'folderId:', selectedFolderId.value)
    const noteData = { 
      title: currentNote.value.title,
      content: currentNote.value.content,
      tags: currentNote.value.tags,
      folderId: selectedFolderId.value
    }
    console.log('发送的更新数据:', noteData)
    await updateNote(currentNote.value.id, noteData)
    ElMessage.success('移动成功')
    showMoveDialog.value = false
    currentNote.value = null
    // 刷新列表
    await fetchNotes()
  } catch (error) {
    console.error('移动失败', error)
    ElMessage.error('移动失败')
  }
}

onMounted(() => {
  fetchNotes()
  fetchFolders()
})

// 监听路由参数变化，重新获取笔记
watch(() => [route.query.folderId, route.query.keyword, route.query.tag], () => {
  fetchNotes()
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
.note-card {
  border-radius: 1rem;
}
</style>
