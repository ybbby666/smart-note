<template>
  <div class="flex-1 flex flex-col h-full bg-white relative overflow-hidden">
    <!-- 编辑器顶部工具栏 -->
    <header class="h-16 border-b border-gray-100 flex items-center justify-between px-6 bg-white sticky top-0 z-20 shadow-sm">
      <div class="flex items-center flex-1 max-w-2xl mr-4">
        <el-button 
          circle 
          icon="ArrowLeft" 
          size="small" 
          @click="router.back()" 
          class="mr-4 shadow-sm hover:shadow-md transition-shadow"
        />
        <el-input 
          v-model="note.title" 
          placeholder="无标题笔记" 
          class="text-xl font-bold border-none bg-transparent hover:bg-gray-50 focus:bg-white transition-all duration-300 rounded-lg"
        />
        <!-- 标签显示 -->
        <div class="ml-4 flex items-center space-x-2">
          <el-tag 
            v-for="tag in note.tags" 
            :key="tag" 
            size="small"
            closable
            @close="handleRemoveTag(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-button 
            size="small" 
            text 
            icon="Plus" 
            @click="showAddTagDialog = true"
          >
            添加标签
          </el-button>
        </div>
      </div>
      <div class="flex items-center space-x-3">
        <div class="flex items-center mr-4">
          <span :class="['w-2 h-2 rounded-full mr-2', saving ? 'bg-orange-400 animate-pulse' : 'bg-green-400']"></span>
          <span class="text-xs text-gray-400 font-medium">{{ saving ? '保存中...' : '已保存' }}</span>
        </div>
        <el-button-group class="shadow-sm">
          <el-button size="small" :icon="Folder" @click="handleOpenMoveDialog">移动到</el-button>
          <el-button size="small" :icon="Star" :type="note.isStarred ? 'warning' : ''" @click="handleToggleStar">收藏</el-button>
          <el-button size="small" icon="MessageSquare" @click="showAnnotationDialog = true">批注</el-button>
          <el-button size="small" :icon="Shield" @click="handleOpenPermissionDialog">权限</el-button>
          <el-button size="small" :icon="BrainCircuit" type="primary" @click="showAiSidebar = !showAiSidebar">AI 助手</el-button>
        </el-button-group>
      </div>
    </header>

    <div class="flex-1 flex overflow-hidden">
      <!-- Markdown 编辑器 -->
      <div :class="['flex-1 transition-all duration-300 overflow-hidden', showAiSidebar ? 'mr-96' : '']">
        <md-editor 
          v-model="note.content" 
          @onSave="handleSave" 
          class="h-full border-none custom-md-editor"
          preview-theme="github"
          theme="light"
          :toolbarsExclude="['github']"
        />
      </div>

      <!-- AI 助手侧边栏 -->
      <aside 
        v-if="showAiSidebar"
        class="w-96 border-l border-gray-200 bg-gray-50/50 flex flex-col absolute right-0 top-0 bottom-0 z-10 shadow-2xl backdrop-blur-sm"
      >
        <div class="p-6 border-b border-gray-200 bg-white flex items-center justify-between">
          <div class="flex items-center">
            <div class="p-2 bg-blue-100 rounded-lg mr-3">
              <BrainCircuit class="w-5 h-5 text-primary" />
            </div>
            <h3 class="text-lg font-bold text-gray-800">AI 智能助手</h3>
          </div>
          <el-button circle size="small" @click="showAiSidebar = false">
            <X class="w-4 h-4" />
          </el-button>
        </div>
        
        <div class="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar">
          <!-- 快捷操作 -->
          <div class="space-y-3">
            <h4 class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-4">快捷智能分析</h4>
            <el-button 
              v-for="action in aiActions" 
              :key="action.title"
              class="w-full text-left justify-start h-12 rounded-xl border-gray-200 hover:border-primary hover:bg-blue-50/50 transition-all duration-300"
              @click="handleAiAction(action.type)"
              :loading="aiLoading === action.type"
            >
              <component :is="action.icon" class="w-4 h-4 mr-3" />
              <span class="font-medium">{{ action.title }}</span>
            </el-button>
          </div>

          <!-- AI 回复内容 -->
          <div v-if="aiResult" class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 animate-fade-in">
            <div class="flex items-center justify-between mb-4">
              <span class="text-sm font-bold text-primary flex items-center">
                <Sparkles class="w-4 h-4 mr-2" />
                智能结果
              </span>
              <el-button type="primary" link size="small" @click="copyAiResult">复制内容</el-button>
            </div>
            <div class="prose prose-sm max-w-none text-gray-600 leading-relaxed whitespace-pre-wrap">
              {{ aiResult }}
            </div>
          </div>

          <!-- 暂无内容占位 -->
          <div v-else-if="!aiLoading" class="flex flex-col items-center justify-center py-20 text-gray-400 text-center px-10">
            <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <Bot class="w-10 h-10 text-gray-300" />
            </div>
            <p class="text-sm font-medium">随时准备为你提供建议</p>
            <p class="text-xs mt-2 text-gray-300 leading-relaxed">你可以尝试让 AI 为你总结全文，或者润色内容</p>
          </div>

          <!-- 加载动画 -->
          <div v-if="aiLoading" class="flex flex-col items-center justify-center py-20 space-y-4">
            <div class="loading-pulse"></div>
            <p class="text-sm text-primary font-medium">正在深度思考中...</p>
          </div>
        </div>
      </aside>
    </div>

    <!-- 添加标签对话框 -->
    <el-dialog v-model="showAddTagDialog" title="添加标签" width="400px">
      <el-input 
        v-model="newTag" 
        placeholder="请输入标签名称" 
        @keyup.enter="handleAddTag"
      />
      <template #footer>
        <el-button @click="showAddTagDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddTag">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批注管理对话框 -->
    <el-dialog v-model="showAnnotationDialog" title="笔记批注" width="600px" @open="fetchAnnotations">
      <div class="space-y-4">
        <!-- 创建批注表单 -->
        <el-form :model="annotationForm" label-width="80px">
          <el-form-item label="选中内容">
            <el-input 
              v-model="annotationForm.selectedText" 
              placeholder="选中的文本（可选）"
            />
          </el-form-item>
          <el-form-item label="批注内容">
            <el-input 
              v-model="annotationForm.content" 
              type="textarea" 
              :rows="3"
              placeholder="请输入批注内容"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleCreateAnnotation">创建批注</el-button>
          </el-form-item>
        </el-form>

        <!-- 批注列表 -->
        <div v-if="annotations.length > 0" class="mt-6 border-t pt-4">
          <h4 class="text-sm font-bold text-gray-700 mb-3">已有批注 ({{ annotations.length }})</h4>
          <div 
            v-for="annotation in annotations" 
            :key="annotation.id"
            class="bg-gray-50 rounded-lg p-4 mb-3"
          >
            <div v-if="annotation.selectedText" class="text-xs text-gray-500 mb-2 bg-white p-2 rounded border-l-4 border-blue-400">
              {{ annotation.selectedText }}
            </div>
            <div class="text-sm text-gray-800 mb-2">{{ annotation.content }}</div>
            <div class="flex items-center justify-between text-xs text-gray-400">
              <span>{{ annotation.createTime }}</span>
              <el-button 
                size="small" 
                type="danger" 
                text 
                @click="handleDeleteAnnotation(annotation.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无批注" :image-size="80" />
      </div>
    </el-dialog>

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

    <!-- 权限管理对话框 -->
    <el-dialog 
      v-model="showPermissionDialog" 
      title="笔记权限设置" 
      width="600px" 
      @open="() => { console.log('对话框打开事件触发'); handleOpenPermissionDialog(); }"
    >
      <el-form :model="permissionForm" label-width="100px">
        <el-form-item label="可见性">
          <el-radio-group v-model="permissionForm.visibility">
            <el-radio value="private">私有（仅自己可见）</el-radio>
            <el-radio value="public">公开（所有人可见）</el-radio>
            <el-radio value="friends">指定好友可见</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="编辑权限">
          <el-switch 
            v-model="permissionForm.canEdit" 
            active-text="允许编辑"
            inactive-text="只读"
          />
        </el-form-item>
        <el-form-item v-if="permissionForm.visibility === 'friends'" label="共享用户">
          <!-- 从好友选择 -->
          <div class="mb-3">
            <el-select 
              v-model="selectedFriendForShare" 
              placeholder="从好友中选择"
              filterable
              class="w-full mb-2"
              @change="(val) => console.log('下拉框选择变化:', val)"
            >
              <el-option
                v-for="friend in friendsList"
                :key="friend.friendId"
                :label="friend.nickname || friend.username"
                :value="friend.friendId"
              >
                <span>{{ friend.nickname || friend.username }}</span>
                <span class="text-gray-400 text-xs ml-2">{{ friend.email }}</span>
              </el-option>
            </el-select>
            <el-button 
              size="small" 
              type="primary" 
              @click="() => { console.log('点击添加按钮, selectedFriendForShare:', selectedFriendForShare); handleAddFriendToShare(); }" 
              :disabled="!selectedFriendForShare"
            >
              添加选中好友
            </el-button>
          </div>
          
          <!-- 或手动输入 -->
          <el-divider content-position="left">或手动输入邮箱</el-divider>
          <el-input 
            v-model="sharedUserEmail" 
            placeholder="输入用户邮箱"
            class="mb-2"
          >
            <template #append>
              <el-button @click="handleAddSharedUser">添加</el-button>
            </template>
          </el-input>
          
          <!-- 已添加的用户列表 -->
          <div v-if="sharedUserDetails && sharedUserDetails.length > 0" class="mt-4">
            <p class="text-sm text-gray-500 mb-2">已添加 {{ sharedUserDetails.length }} 个用户：</p>
            <div class="space-y-2">
              <el-tag 
                v-for="(user, index) in sharedUserDetails" 
                :key="user.id"
                closable
                size="large"
                @close="handleRemoveSharedUser(index)"
              >
                {{ user.nickname || user.username || user.email }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="暂未添加共享用户" :image-size="60" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPermissionDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermission" :loading="savingPermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { 
  ArrowLeft, 
  Folder,
  Star, 
  BrainCircuit, 
  FileSearch, 
  Sparkles, 
  Bot, 
  Languages, 
  Wand2,
  X,
  MessageSquare,
  Plus,
  Shield
} from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { getNoteDetail, createNote, updateNote, aiAnalyzeNote, createAnnotation, getAnnotations, deleteAnnotation } from '@/api/note'
import { getFolderTree } from '@/api/folder'
import { getNotePermission, updateNotePermission } from '@/api/notePermission'
import { getFriends } from '@/api/friend'

const route = useRoute()
const router = useRouter()
const showAiSidebar = ref(false)
const saving = ref(false)
const aiLoading = ref('')
const aiResult = ref('')

const note = ref({
  id: null,
  title: '',
  content: '',
  isStarred: false,
  folderId: null,
  tags: []
})

const showAddTagDialog = ref(false)
const showAnnotationDialog = ref(false)
const showMoveDialog = ref(false)
const showPermissionDialog = ref(false)
const savingPermission = ref(false)
const sharedUserEmail = ref('')
const selectedFriendForShare = ref('') // 选中的好友用于共享
const friendsList = ref([]) // 好友列表
const sharedUserDetails = ref([]) // 已共享用户的详细信息
const newTag = ref('')
const annotations = ref([])
const folders = ref([])
const selectedFolderId = ref(null)
const annotationForm = ref({
  selectedText: '',
  content: ''
})
const permissionForm = ref({
  visibility: 'private',
  canEdit: false,
  sharedUsers: []
})

const aiActions = [
  { title: '总结全文摘要', icon: FileSearch, type: 'summary' },
  { title: '内容润色优化', icon: Wand2, type: 'refine' },
  { title: '多语言翻译', icon: Languages, type: 'translate' }
]

const fetchNoteDetail = async () => {
  if (route.params.id === 'new') {
    note.value = { id: null, title: '', content: '', isStarred: false }
    return
  }
  try {
    const data = await getNoteDetail(route.params.id)
    note.value = data
  } catch (error) {
    console.error('获取笔记失败', error)
  }
}

const handleSave = async () => {
  console.log('开始保存笔记, note:', note.value)
  saving.value = true
  try {
    if (note.value.id) {
      console.log('更新现有笔记, ID:', note.value.id)
      await updateNote(note.value.id, note.value)
      ElMessage.success('保存成功')
    } else {
      console.log('创建新笔记')
      const data = await createNote(note.value)
      console.log('创建笔记返回数据:', data)
      // 后端返回的可能是 {code:1, msg:'success', data:noteId}
      const newId = data?.id || data
      console.log('新笔记ID:', newId)
      if (!newId || newId === 'null' || newId === 'undefined') {
        throw new Error('创建笔记失败：未返回有效的ID')
      }
      note.value.id = newId
      router.replace(`/note/${newId}`)
      ElMessage.success('创建成功')
    }
  } catch (error) {
    console.error('保存失败', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    setTimeout(() => { saving.value = false }, 500)
  }
}

const handleToggleStar = async () => {
  note.value.isStarred = !note.value.isStarred
  handleSave()
}

const handleAiAction = async (type) => {
  if (!note.value.content) {
    ElMessage.warning('笔记内容为空，无法进行 AI 分析')
    return
  }
  aiLoading.value = type
  aiResult.value = ''
  try {
    // 如果是新笔记，先保存
    if (!note.value.id) {
      await handleSave()
    }
    const data = await aiAnalyzeNote(note.value.id)
    aiResult.value = data.result || data.summary || JSON.stringify(data)
  } catch (error) {
    console.error('AI 分析失败', error)
    ElMessage.error('AI 分析服务暂时不可用，请检查：\n1. API密钥是否有效\n2. 网络连接是否正常\n3. AI服务是否可用')
  } finally {
    aiLoading.value = ''
  }
}

const copyAiResult = () => {
  navigator.clipboard.writeText(aiResult.value)
  ElMessage.success('已复制到剪贴板')
}

const handleShare = () => {
  ElMessage.info('分享功能即将上线')
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
const handleOpenMoveDialog = () => {
  console.log('点击移动到按钮, note:', note.value)
  console.log('当前文件夹ID:', note.value.folderId)
  console.log('文件夹列表:', folders.value)
  // 设置当前选中的文件夹
  selectedFolderId.value = note.value.folderId || null
  showMoveDialog.value = true
  console.log('对话框状态:', showMoveDialog.value)
}

// 移动到文件夹
const handleMoveToFolder = async () => {
  if (!note.value.id) {
    ElMessage.warning('请先保存笔记')
    return
  }
  try {
    // 创建更新数据副本，确保folderId字段正确
    const noteData = { ...note.value }
    noteData.folderId = selectedFolderId.value
    console.log('移动笔记到文件夹:', note.value.id, 'folderId:', selectedFolderId.value)
    await updateNote(note.value.id, noteData)
    ElMessage.success('移动成功')
    showMoveDialog.value = false
  } catch (error) {
    console.error('移动失败', error)
    ElMessage.error('移动失败')
  }
}

// 标签管理
const handleAddTag = async () => {
  if (!newTag.value.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  if (!note.value.tags) {
    note.value.tags = []
  }
  if (note.value.tags.includes(newTag.value)) {
    ElMessage.warning('标签已存在')
    return
  }
  note.value.tags.push(newTag.value)
  newTag.value = ''
  showAddTagDialog.value = false
  await handleSave()
  ElMessage.success('标签添加成功')
}

const handleRemoveTag = async (tag) => {
  note.value.tags = note.value.tags.filter(t => t !== tag)
  await handleSave()
  ElMessage.success('标签删除成功')
}

// 批注管理
const fetchAnnotations = async () => {
  if (!note.value.id) return
  try {
    const data = await getAnnotations(note.value.id)
    // 将后端的 position 字段映射为 selectedText
    annotations.value = (data || []).map(item => ({
      ...item,
      selectedText: item.position || ''
    }))
  } catch (error) {
    console.error('获取批注失败', error)
  }
}

const handleCreateAnnotation = async () => {
  if (!annotationForm.value.content.trim()) {
    ElMessage.warning('请输入批注内容')
    return
  }
  try {
    // 将 selectedText 映射为 position
    const data = {
      content: annotationForm.value.content,
      position: annotationForm.value.selectedText || ''
    }
    await createAnnotation(note.value.id, data)
    ElMessage.success('批注创建成功')
    annotationForm.value = { selectedText: '', content: '' }
    showAnnotationDialog.value = false
    await fetchAnnotations()
  } catch (error) {
    console.error('创建批注失败', error)
  }
}

const handleDeleteAnnotation = async (annotationId) => {
  try {
    await deleteAnnotation(annotationId)
    ElMessage.success('批注删除成功')
    await fetchAnnotations()
  } catch (error) {
    console.error('删除批注失败', error)
  }
}

onMounted(() => {
  fetchNoteDetail()
  fetchFolders()
})

// 打开权限对话框
const handleOpenPermissionDialog = () => {
  console.log('打开权限对话框')
  showPermissionDialog.value = true
  // 加载好友列表
  if (friendsList.value.length === 0) {
    console.log('好友列表为空，开始加载...')
    fetchFriendsList()
  } else {
    console.log('好友列表已加载:', friendsList.value)
  }
  // 获取当前笔记的权限设置
  fetchPermission()
}

// 获取好友列表
const fetchFriendsList = async () => {
  try {
    const data = await getFriends({ pageNum: 1, pageSize: 100 })
    friendsList.value = data.records || []
    console.log('获取到的好友列表:', friendsList.value)
    console.log('好友列表详情:', JSON.stringify(friendsList.value, null, 2))
  } catch (error) {
    console.error('获取好友列表失败', error)
  }
}

// 从好友中添加共享用户
const handleAddFriendToShare = () => {
  console.log('当前选中的好友ID:', selectedFriendForShare.value)
  console.log('好友列表:', friendsList.value)
  
  if (!selectedFriendForShare.value) {
    ElMessage.warning('请选择一个好友')
    return
  }
  if (!permissionForm.value.sharedUsers) {
    permissionForm.value.sharedUsers = []
  }
  if (permissionForm.value.sharedUsers.includes(selectedFriendForShare.value)) {
    ElMessage.warning('该好友已添加')
    return
  }
  
  // 添加到 ID 列表
  permissionForm.value.sharedUsers.push(selectedFriendForShare.value)
  
  // 添加到详细信息列表
  const friend = friendsList.value.find(f => f.friendId === selectedFriendForShare.value)
  console.log('找到的好友对象:', friend)
  
  if (friend) {
    if (!sharedUserDetails.value) {
      sharedUserDetails.value = []
    }
    sharedUserDetails.value.push({
      id: friend.friendId,
      username: friend.username,
      email: friend.email,
      nickname: friend.nickname
    })
  }
  
  console.log('更新后的共享用户列表:', permissionForm.value.sharedUsers)
  console.log('更新后的共享用户详情:', sharedUserDetails.value)
  
  selectedFriendForShare.value = '' // 清空选择
  ElMessage.success('添加成功')
}

// 获取笔记权限
const fetchPermission = async () => {
  if (!note.value.id) {
    ElMessage.warning('请先保存笔记')
    return
  }
  try {
    const data = await getNotePermission(note.value.id)
    // 将后端数据格式转换为前端期望的格式
    let visibility = 'private'
    let canEdit = false
    
    if (data.permissionType === 3) {
      visibility = 'public' // TYPE_PUBLIC
    } else if (data.permissionType === 1 || data.permissionType === 2) {
      visibility = 'friends'
      canEdit = data.permissionType === 2 // TYPE_FRIEND_EDIT
    }
    
    permissionForm.value = {
      visibility: visibility,
      canEdit: canEdit,
      sharedUsers: data.shareUserIdList || []
    }
    
    // 保存用户详细信息用于显示
    sharedUserDetails.value = data.shareUserList || []
  } catch (error) {
    console.error('获取权限失败', error)
    ElMessage.error('获取权限失败')
  }
}

// 添加共享用户
const handleAddSharedUser = () => {
  if (!sharedUserEmail.value.trim()) {
    ElMessage.warning('请输入邮箱')
    return
  }
  if (!permissionForm.value.sharedUsers) {
    permissionForm.value.sharedUsers = []
  }
  if (permissionForm.value.sharedUsers.includes(sharedUserEmail.value)) {
    ElMessage.warning('该用户已添加')
    return
  }
  permissionForm.value.sharedUsers.push(sharedUserEmail.value)
  sharedUserEmail.value = ''
}

// 移除共享用户
const handleRemoveSharedUser = (index) => {
  const userId = sharedUserDetails.value[index].id
  sharedUserDetails.value.splice(index, 1)
  permissionForm.value.sharedUsers = permissionForm.value.sharedUsers.filter(id => id !== userId)
}

// 保存权限设置
const handleSavePermission = async () => {
  if (!note.value.id) {
    ElMessage.warning('请先保存笔记')
    return
  }
  
  savingPermission.value = true
  try {
    // 将前端数据格式转换为后端期望的格式
    let permissionType = 0 // 默认私有
    if (permissionForm.value.visibility === 'public') {
      permissionType = 3 // TYPE_PUBLIC
    } else if (permissionForm.value.visibility === 'friends') {
      if (permissionForm.value.canEdit) {
        permissionType = 2 // TYPE_FRIEND_EDIT
      } else {
        permissionType = 1 // TYPE_FRIEND_READ
      }
    }
    
    const requestData = {
      permissionType: permissionType,
      shareUserIdList: permissionForm.value.sharedUsers || []
    }
    
    await updateNotePermission(note.value.id, requestData)
    ElMessage.success('权限设置成功')
    showPermissionDialog.value = false
  } catch (error) {
    console.error('保存权限失败', error)
    ElMessage.error(error.message || '保存权限失败')
  } finally {
    savingPermission.value = false
  }
}

watch(() => route.params.id, () => {
  fetchNoteDetail()
})
</script>

<style scoped>
.custom-md-editor {
  --md-bk-color: transparent;
}
.animate-fade-in {
  animation: fadeIn 0.4s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.loading-pulse {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #409eff;
  animation: pulse 1.5s infinite;
  opacity: 0.6;
}
@keyframes pulse {
  0% { transform: scale(0.8); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 0.3; }
  100% { transform: scale(0.8); opacity: 0.8; }
}
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}
</style>
