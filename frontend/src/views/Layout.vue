<template>
  <div class="layout-container h-screen flex bg-gray-50 overflow-hidden">
    <!-- 侧边栏 -->
    <aside class="w-64 bg-white border-r border-gray-200 flex flex-col shadow-sm">
      <div class="p-6 border-b border-gray-100 flex items-center justify-between">
        <h1 class="text-xl font-bold text-primary tracking-tight">SmartNote</h1>
        <el-button type="primary" circle size="small" icon="Plus" @click="handleNewNote" />
      </div>

      <!-- 搜索 -->
      <div class="px-4 py-3">
        <el-input 
          v-model="searchText" 
          placeholder="搜索笔记..." 
          prefix-icon="Search"
          class="rounded-lg"
          size="small"
          @keyup.enter="handleSearch"
        />
      </div>

      <!-- 导航菜单 -->
      <nav class="flex-1 overflow-y-auto px-2 py-4 space-y-1">
        <div 
          v-for="item in menuItems" 
          :key="item.id"
          @click="handleMenuClick(item.id)"
          :class="[
            'flex items-center px-4 py-2 text-sm font-medium rounded-lg cursor-pointer transition-colors duration-200',
            currentMenu === item.id ? 'bg-blue-50 text-primary' : 'text-gray-600 hover:bg-gray-100'
          ]"
        >
          <component :is="item.icon" class="w-5 h-5 mr-3" />
          {{ item.title }}
        </div>

        <div class="mt-8 px-4 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wider flex items-center justify-between">
          <span>文件夹</span>
          <el-button 
            size="small" 
            circle 
            :icon="Plus"
            @click="showNewFolderDialog = true"
            class="text-gray-400 hover:text-primary"
          />
        </div>
        
        <div 
          v-for="folder in folders" 
          :key="folder.id"
          @click="handleSelectFolder(folder.id)"
          :class="[
            'flex items-center px-4 py-2 text-sm rounded-lg cursor-pointer transition-colors duration-200',
            selectedFolderId === folder.id ? 'bg-blue-50 text-primary' : 'text-gray-600 hover:bg-gray-100'
          ]"
        >
          <Folder class="w-5 h-5 mr-3" :class="selectedFolderId === folder.id ? 'text-primary' : 'text-gray-400'" />
          {{ folder.name }}
        </div>
      </nav>

      <!-- 用户信息 -->
      <div class="p-4 border-t border-gray-100 bg-gray-50/50">
        <el-dropdown trigger="click">
          <div class="flex items-center cursor-pointer">
            <el-avatar :size="32" :src="userInfo.avatar || ''" class="mr-3">{{ userInfo.nickname?.charAt(0) || userInfo.username?.charAt(0) }}</el-avatar>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">{{ userInfo.nickname || userInfo.username }}</p>
              <p class="text-xs text-gray-500 truncate">{{ userInfo.email }}</p>
            </div>
            <MoreHorizontal class="w-4 h-4 ml-2 text-gray-400" />
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item icon="User" @click="openProfileDialog">个人中心</el-dropdown-item>
              <el-dropdown-item :icon="Settings" @click="showSettingsDialog = true">设置</el-dropdown-item>
              <el-dropdown-item divided :icon="LogOut" @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <router-view />
    </main>

    <!-- 新建文件夹对话框 -->
    <el-dialog v-model="showNewFolderDialog" title="新建文件夹" width="400px">
      <el-form :model="newFolderForm" label-width="80px">
        <el-form-item label="文件夹名称">
          <el-input 
            v-model="newFolderForm.name" 
            placeholder="请输入文件夹名称"
            @keyup.enter="handleCreateFolder"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNewFolderDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateFolder">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑个人资料对话框 -->
    <el-dialog v-model="showProfileDialog" title="编辑个人资料" width="500px">
      <el-form :model="profileForm" label-width="80px">
        <el-form-item label="头像">
          <div class="flex items-center space-x-4">
            <el-avatar :size="80" :src="previewAvatar || profileForm.avatar || ''" />
            <div class="flex-1">
              <el-upload
                class="avatar-uploader"
                action="/api/avatar"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :headers="uploadHeaders"
              >
                <el-button size="small" type="primary">选择图片</el-button>
              </el-upload>
              <p class="text-xs text-gray-400 mt-2">支持 JPG、PNG 格式，大小不超过 2MB</p>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input 
            v-model="profileForm.nickname" 
            placeholder="请输入昵称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="座右铭">
          <el-input 
            v-model="profileForm.motto" 
            type="textarea"
            :rows="3"
            placeholder="请输入座右铭"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProfile" :loading="updatingProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 设置对话框 -->
    <el-dialog v-model="showSettingsDialog" title="设置" width="500px">
      <el-tabs v-model="settingsTab">
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="passwordForm" label-width="100px" class="mt-4">
            <el-form-item label="旧密码">
              <el-input 
                v-model="passwordForm.oldPassword" 
                type="password"
                placeholder="请输入旧密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password"
                placeholder="请输入新密码（6-16位）"
                show-password
                maxlength="16"
              />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password"
                placeholder="请再次输入新密码"
                show-password
                maxlength="16"
                @keyup.enter="handleChangePassword"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="关于" name="about">
          <div class="py-8 text-center">
            <h3 class="text-xl font-bold text-primary mb-2">SmartNote</h3>
            <p class="text-gray-500">智能笔记，随手记录</p>
            <p class="text-sm text-gray-400 mt-4">版本 v1.0.0</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  FileText, 
  Star, 
  Trash2, 
  Folder, 
  MoreHorizontal,
  Plus,
  Search,
  User,
  Settings,
  LogOut,
  Users,
  Clock
} from 'lucide-vue-next'
import { getFolderTree, createFolder } from '@/api/folder'
import { getMyProfile, updateProfile, updatePassword } from '@/api/user'

const router = useRouter()
// 安全地解析用户信息
let userInfoData = {}
try {
  const stored = localStorage.getItem('userInfo')
  userInfoData = stored ? JSON.parse(stored) : {}
} catch (error) {
  console.error('解析用户信息失败', error)
  userInfoData = {}
}
const userInfo = ref(userInfoData)
const currentMenu = ref('all')
const searchText = ref('')
const folders = ref([])
const selectedFolderId = ref(null) // 当前选中的文件夹ID
const showNewFolderDialog = ref(false)
const showProfileDialog = ref(false)
const showSettingsDialog = ref(false)
const settingsTab = ref('security')
const newFolderForm = ref({ name: '' })
const updatingProfile = ref(false)
const changingPassword = ref(false)
const previewAvatar = ref('') // 头像预览
const uploadHeaders = ref({}) // 上传请求头
const profileForm = ref({
  nickname: '',
  motto: '',
  avatar: ''
})
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const menuItems = [
  { id: 'all', title: '全部笔记', icon: FileText },
  { id: 'starred', title: '收藏', icon: Star },
  { id: 'history', title: '浏览历史', icon: Clock },
  { id: 'friends', title: '好友', icon: Users },
  { id: 'trash', title: '回收站', icon: Trash2 }
]

const handleNewNote = () => {
  console.log('侧边栏点击创建笔记')
  router.push('/note/new')
}

const handleSearch = () => {
  if (!searchText.value.trim()) {
    // 如果搜索框为空，跳转到全部笔记
    router.push({ path: '/' })
  } else {
    // 跳转到首页并传递搜索关键词
    router.push({ path: '/', query: { keyword: searchText.value.trim() } })
  }
}

const handleMenuClick = (menuId) => {
  currentMenu.value = menuId
  selectedFolderId.value = null // 清除文件夹选中状态
  // 如果点击回收站,跳转到回收站页面
  if (menuId === 'trash') {
    router.push('/recycle-bin')
  } else if (menuId === 'all') {
    // 全部笔记，不传 folderId 参数，显示所有可看的笔记
    router.push({ path: '/' })
  } else if (menuId === 'friends') {
    // 跳转到好友页面
    router.push('/friends')
  } else if (menuId === 'history') {
    // 跳转到浏览历史页面
    router.push('/history')
  }
  // 其他菜单项可以后续扩展
}

const handleSelectFolder = (folderId) => {
  console.log('点击文件夹:', folderId)
  selectedFolderId.value = folderId
  currentMenu.value = '' // 清除菜单选中状态
  
  // 判断是否是默认文件夹（第一个文件夹）
  const isDefaultFolder = folders.value.length > 0 && folders.value[0].id === folderId
  
  if (isDefaultFolder) {
    // 默认文件夹：显示未归类到任何文件夹的笔记
    router.push({ path: '/', query: { folderId: 0 } })
  } else {
    // 其他文件夹：显示该文件夹下的笔记
    router.push({ path: '/', query: { folderId } })
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    console.log('执行退出登录')
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消操作
    console.log('取消退出')
  }
}

// 打开个人资料对话框时加载数据
const openProfileDialog = async () => {
  showProfileDialog.value = true
  previewAvatar.value = '' // 清空预览
  
  // 设置上传请求头
  const token = localStorage.getItem('token')
  if (token) {
    uploadHeaders.value = {
      'Authorization': `Bearer ${token}`
    }
  }
  
  try {
    const data = await getMyProfile()
    profileForm.value = {
      nickname: data.nickname || '',
      motto: data.motto || '',
      avatar: data.avatar || ''
    }
  } catch (error) {
    console.error('获取个人资料失败', error)
    ElMessage.error('获取个人资料失败')
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.code === 1) {
    const avatarUrl = response.data
    profileForm.value.avatar = avatarUrl
    previewAvatar.value = avatarUrl // 立即预览
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.msg || '头像上传失败')
  }
}

// 修改密码
const handleChangePassword = async () => {
  // 验证输入
  if (!passwordForm.value.oldPassword) {
    ElMessage.warning('请输入旧密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword.length < 6 || passwordForm.value.newPassword.length > 16) {
    ElMessage.warning('密码长度必须在6-16位之间')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  changingPassword.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
      confirmNewPassword: passwordForm.value.confirmPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    showSettingsDialog.value = false
    // 清空表单
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    // 退出登录
    setTimeout(() => {
      handleLogout()
    }, 1500)
  } catch (error) {
    console.error('修改密码失败', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

// 更新个人资料
const handleUpdateProfile = async () => {
  if (!profileForm.value.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  
  updatingProfile.value = true
  try {
    await updateProfile(profileForm.value)
    ElMessage.success('资料更新成功')
    showProfileDialog.value = false
    
    // 更新本地存储的用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    userInfo.nickname = profileForm.value.nickname
    userInfo.motto = profileForm.value.motto
    userInfo.avatar = profileForm.value.avatar
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
    
    // 刷新页面显示
    userInfoData = userInfo
    userInfo.value = userInfo
  } catch (error) {
    console.error('更新资料失败', error)
    ElMessage.error(error.message || '更新资料失败')
  } finally {
    updatingProfile.value = false
  }
}

const fetchFolders = async () => {
  try {
    const data = await getFolderTree()
    folders.value = data || []
  } catch (error) {
    console.error('获取文件夹失败', error)
    folders.value = []
  }
}

const handleCreateFolder = async () => {
  if (!newFolderForm.value.name.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  try {
    await createFolder({ name: newFolderForm.value.name })
    ElMessage.success('文件夹创建成功')
    showNewFolderDialog.value = false
    newFolderForm.value.name = ''
    await fetchFolders() // 刷新文件夹列表
  } catch (error) {
    console.error('创建文件夹失败', error)
    ElMessage.error('创建文件夹失败')
  }
}

onMounted(() => {
  fetchFolders()
})
</script>

<style scoped>
.layout-container {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
</style>
