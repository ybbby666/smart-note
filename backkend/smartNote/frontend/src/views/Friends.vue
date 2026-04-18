<template>
  <div class="flex-1 flex flex-col h-full bg-white overflow-hidden">
    <!-- 头部 -->
    <header class="h-16 border-b border-gray-100 flex items-center justify-between px-8 bg-white/80 backdrop-blur-md sticky top-0 z-10 shadow-sm">
      <h2 class="text-xl font-bold text-gray-800">好友管理</h2>
      <el-button type="primary" :icon="UserPlus" @click="showSearchDialog = true">添加好友</el-button>
    </header>

    <!-- Tab切换 -->
    <div class="border-b border-gray-200">
      <el-tabs v-model="activeTab" class="px-8">
        <el-tab-pane label="我的好友" name="friends">
          <span class="ml-2 text-sm text-gray-500">({{ friends.length }})</span>
        </el-tab-pane>
        <el-tab-pane label="好友申请" name="requests">
          <el-badge :value="pendingRequestsCount" :hidden="pendingRequestsCount === 0" class="ml-2">
            <span class="text-sm text-gray-500"></span>
          </el-badge>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 好友列表 -->
    <div v-if="activeTab === 'friends'" class="flex-1 overflow-y-auto p-8 custom-scrollbar bg-gray-50/30">
      <div v-if="groupedFriends.length > 0" class="space-y-4">
        <!-- 按分组显示 -->
        <div v-for="group in groupedFriends" :key="group.name" class="bg-white rounded-xl shadow-sm overflow-hidden">
          <!-- 分组标题 -->
          <div 
            @click="toggleGroup(group.name)"
            class="px-4 py-3 bg-gray-50 border-b border-gray-100 flex items-center justify-between cursor-pointer hover:bg-gray-100 transition-colors"
          >
            <div class="flex items-center space-x-2">
              <component 
                :is="expandedGroups.includes(group.name) ? ChevronDown : ChevronRight" 
                class="w-4 h-4 text-gray-500"
              />
              <span class="font-semibold text-gray-700">{{ group.name }}</span>
              <el-tag size="small" type="info" effect="plain">{{ group.friends.length }}</el-tag>
            </div>
          </div>
          
          <!-- 分组下的好友列表 -->
          <div v-show="expandedGroups.includes(group.name)" class="divide-y divide-gray-50">
            <div 
              v-for="friend in group.friends" 
              :key="friend.friendId"
              class="p-4 flex items-center justify-between hover:bg-gray-50 transition-colors"
            >
              <div class="flex items-center space-x-4">
                <el-avatar :size="45" :src="friend.avatar || '/default-avatar.png'" />
                <div>
                  <h3 class="font-semibold text-gray-800">{{ friend.nickname || friend.username }}</h3>
                  <p class="text-sm text-gray-500">{{ friend.motto || friend.email }}</p>
                </div>
              </div>
              <el-dropdown trigger="click">
                <el-button circle size="small" :icon="MoreHorizontal" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEditGroup(friend)">修改分组</el-dropdown-item>
                    <el-dropdown-item divided class="text-red-500">删除好友</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无好友，快去添加吧！" :image-size="200" />
    </div>

    <!-- 好友申请列表 -->
    <div v-if="activeTab === 'requests'" class="flex-1 overflow-y-auto p-8 custom-scrollbar bg-gray-50/30">
      <div v-if="requests.length > 0" class="space-y-3">
        <div 
          v-for="request in requests" 
          :key="request.applyId"
          class="bg-white rounded-xl p-4 shadow-sm hover:shadow-md transition-all duration-300"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <el-avatar :size="50" :src="request.applyAvatar || '/default-avatar.png'" />
              <div>
                <h3 class="font-semibold text-gray-800">{{ request.applyUsername }}</h3>
                <p class="text-sm text-gray-500">{{ request.message || '请求添加您为好友' }}</p>
                <p class="text-xs text-gray-400 mt-1">{{ formatTime(request.createTime) }}</p>
              </div>
            </div>
            <div v-if="request.status === 0" class="flex items-center space-x-2">
              <el-button size="small" type="success" @click="handleRequest(request.applyId, 1)">接受</el-button>
              <el-button size="small" type="danger" @click="handleRequest(request.applyId, 2)">拒绝</el-button>
            </div>
            <el-tag v-else :type="request.status === 1 ? 'success' : 'info'" size="small">
              {{ request.status === 1 ? '已接受' : '已拒绝' }}
            </el-tag>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无好友申请" :image-size="200" />
    </div>

    <!-- 搜索好友对话框 -->
    <el-dialog v-model="showSearchDialog" title="添加好友" width="500px">
      <el-input 
        v-model="searchKeyword" 
        placeholder="输入用户名或邮箱搜索"
        :prefix-icon="Search"
        @keyup.enter="handleSearchOrAdd"
      />
      <div v-if="searchResult" class="mt-4 p-4 bg-gray-50 rounded-lg">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <el-avatar :size="40" :src="searchResult.avatar || '/default-avatar.png'" />
            <div>
              <p class="font-medium">{{ searchResult.username }}</p>
              <p class="text-sm text-gray-500">{{ searchResult.email }}</p>
            </div>
          </div>
          <el-tag v-if="searchResult.isFriend" type="success" size="small">已是好友</el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="showSearchDialog = false; searchKeyword = ''; searchResult = null">取消</el-button>
        <el-button 
          type="primary" 
          :disabled="!searchKeyword.trim() || (searchResult && searchResult.isFriend)"
          @click="handleSearchOrAdd"
        >
          {{ searchResult ? '确定添加' : '搜索' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改分组对话框 -->
    <el-dialog v-model="showGroupDialog" title="修改分组" width="400px">
      <el-input 
        v-model="newGroupName" 
        placeholder="请输入分组名称"
      />
      <template #footer>
        <el-button @click="showGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmEditGroup">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UserPlus, MoreHorizontal, Search, ChevronDown, ChevronRight } from 'lucide-vue-next'
import dayjs from 'dayjs'
import { 
  getFriends, 
  getFriendRequests, 
  searchFriend, 
  sendFriendRequest, 
  handleFriendRequest,
  updateFriendGroup 
} from '@/api/friend'

const activeTab = ref('friends')
const friends = ref([])
const requests = ref([])
const showSearchDialog = ref(false)
const showGroupDialog = ref(false)
const searchKeyword = ref('')
const searchResult = ref(null)
const newGroupName = ref('')
const currentFriend = ref(null)
const expandedGroups = ref(['默认分组']) // 默认展开的分组

// 待处理申请数量
const pendingRequestsCount = computed(() => {
  return requests.value.filter(r => r.status === 0).length
})

// 按分组组织好友列表
const groupedFriends = computed(() => {
  const groups = {}
  friends.value.forEach(friend => {
    const groupName = friend.groupName || '默认分组'
    if (!groups[groupName]) {
      groups[groupName] = {
        name: groupName,
        friends: []
      }
    }
    groups[groupName].friends.push(friend)
  })
  return Object.values(groups)
})

// 切换分组展开/折叠
const toggleGroup = (groupName) => {
  const index = expandedGroups.value.indexOf(groupName)
  if (index > -1) {
    expandedGroups.value.splice(index, 1)
  } else {
    expandedGroups.value.push(groupName)
  }
}

// 获取好友列表
const fetchFriends = async () => {
  try {
    const data = await getFriends({ pageNum: 1, pageSize: 100 })
    friends.value = data.records || []
  } catch (error) {
    console.error('获取好友列表失败', error)
    ElMessage.error('获取好友列表失败')
  }
}

// 获取好友申请列表
const fetchRequests = async () => {
  try {
    const data = await getFriendRequests({ pageNum: 1, pageSize: 100 })
    requests.value = data.records || []
  } catch (error) {
    console.error('获取好友申请失败', error)
  }
}

// 搜索好友
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  console.log('开始搜索:', searchKeyword.value)
  try {
    const data = await searchFriend(searchKeyword.value)
    console.log('搜索结果:', data)
    
    // 如果返回 null，说明搜索的是自己或未找到
    if (!data) {
      ElMessage.warning('未找到该用户或不能添加自己为好友')
      searchResult.value = null
      return
    }
    
    searchResult.value = data
  } catch (error) {
    console.error('搜索失败', error)
    ElMessage.error('未找到该用户')
    searchResult.value = null
  }
}

// 搜索或添加好友（统一处理）
const handleSearchOrAdd = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  // 如果已经搜索出结果，则发送好友申请
  if (searchResult.value && !searchResult.value.isFriend) {
    await handleSendRequest(searchResult.value.friendId)
  } else {
    // 否则执行搜索
    await handleSearch()
  }
}

// 发送好友申请
const handleSendRequest = async (targetUserId) => {
  try {
    await sendFriendRequest(targetUserId)
    ElMessage.success('好友申请已发送')
    showSearchDialog.value = false
    searchKeyword.value = ''
    searchResult.value = null
  } catch (error) {
    console.error('发送申请失败', error)
    ElMessage.error(error.message || '发送申请失败')
  }
}

// 处理好友申请
const handleRequest = async (applyId, status) => {
  try {
    await handleFriendRequest(applyId, status)
    ElMessage.success(status === 1 ? '已接受' : '已拒绝')
    await fetchRequests()
    if (status === 1) {
      await fetchFriends()
    }
  } catch (error) {
    console.error('处理申请失败', error)
    ElMessage.error('处理失败')
  }
}

// 打开修改分组对话框
const handleEditGroup = (friend) => {
  currentFriend.value = friend
  newGroupName.value = friend.groupName || ''
  showGroupDialog.value = true
}

// 确认修改分组
const handleConfirmEditGroup = async () => {
  if (!currentFriend.value) return
  try {
    await updateFriendGroup(currentFriend.value.friendId, newGroupName.value)
    ElMessage.success('分组修改成功')
    showGroupDialog.value = false
    await fetchFriends()
  } catch (error) {
    console.error('修改分组失败', error)
    ElMessage.error('修改分组失败')
  }
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchFriends()
  fetchRequests()
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
