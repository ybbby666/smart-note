# SmartNote 前端项目

## 项目简介
SmartNote 是一个智能笔记应用的前端部分,基于 Vue 3 + Vite 构建,提供现代化的笔记管理体验。

## 技术栈
- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **样式**: Tailwind CSS
- **Markdown编辑器**: md-editor-v3
- **图标**: Lucide Vue Next
- **工具库**: @vueuse/core

## 项目结构
```
frontend/
├── src/
│   ├── api/              # API接口模块
│   │   ├── index.js      # 统一导出
│   │   ├── user.js       # 用户相关API
│   │   ├── note.js       # 笔记相关API
│   │   ├── folder.js     # 文件夹相关API
│   │   ├── friend.js     # 好友相关API
│   │   └── recycleBin.js # 回收站相关API
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── store/            # Pinia状态管理
│   │   └── user.js       # 用户状态
│   ├── styles/           # 全局样式
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios封装
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Layout.vue    # 布局页
│   │   ├── Home.vue      # 首页(笔记列表)
│   │   └── NoteDetail.vue # 笔记详情页
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── package.json
├── vite.config.js        # Vite配置
├── tailwind.config.js    # Tailwind配置
└── postcss.config.js     # PostCSS配置
```

## 快速开始

### 安装依赖
```bash
cd frontend
npm install
```

### 启动开发服务器
```bash
npm run dev
```
访问 http://localhost:5173

### 构建生产版本
```bash
npm run build
```

### 预览生产构建
```bash
npm run preview
```

## API模块说明

### 用户API (api/user.js)
- `login(data)` - 用户登录
- `emailLogin(data)` - 邮箱验证码登录
- `register(data)` - 用户注册
- `logout()` - 用户登出
- `refreshToken(refreshToken)` - 刷新Token
- `getMyProfile()` - 获取当前用户资料
- `getUserProfile(userId)` - 获取指定用户资料
- `updateProfile(data)` - 更新用户资料
- `updatePassword(data)` - 修改密码
- `sendVerificationCode(data)` - 发送验证码
- `resetPassword(data)` - 重置密码

### 笔记API (api/note.js)
- `createNote(data)` - 创建笔记
- `getNoteList(params)` - 分页查询笔记列表
- `getNoteDetail(id)` - 获取笔记详情
- `updateNote(id, data)` - 更新笔记
- `deleteNote(id)` - 删除笔记
- `getNoteHistory(params)` - 获取笔记历史
- `aiAnalyzeNote(noteId)` - AI分析笔记
- `getAiAnalysis(noteId)` - 获取AI分析结果

### 文件夹API (api/folder.js)
- `createFolder(data)` - 创建文件夹
- `renameFolder(folderId, data)` - 重命名文件夹
- `deleteFolder(folderId)` - 删除文件夹
- `getFolderTree()` - 获取文件夹树

### 好友API (api/friend.js)
- `searchFriend(keyword)` - 搜索好友
- `sendFriendRequest(targetUserId)` - 发送好友申请
- `handleFriendRequest(applyId, status)` - 处理好友申请
- `getFriends(params)` - 获取好友列表
- `updateFriendGroup(friendId, groupName)` - 更新好友分组
- `getFriendRequests(params)` - 获取好友申请列表

### 回收站API (api/recycleBin.js)
- `getRecycleBinList()` - 获取回收站列表
- `restoreFromRecycleBin(recycleBinId)` - 从回收站恢复
- `permanentDelete(recycleBinId)` - 彻底删除
- `clearRecycleBin()` - 清空回收站

## 使用示例

### 在组件中使用API
```vue
<script setup>
import { ref, onMounted } from 'vue'
import { getNoteList, deleteNote } from '@/api/note'

const notes = ref([])

// 获取笔记列表
const fetchNotes = async () => {
  const data = await getNoteList({ pageNum: 1, pageSize: 10 })
  notes.value = data.records
}

// 删除笔记
const handleDelete = async (id) => {
  await deleteNote(id)
  fetchNotes()
}

onMounted(() => {
  fetchNotes()
})
</script>
```

### 使用Pinia Store
```vue
<script setup>
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 登录
const handleLogin = async () => {
  await userStore.login({ username: 'admin', password: '123456' })
}

// 登出
const handleLogout = async () => {
  await userStore.logout()
}
</script>
```

## 代理配置
开发环境下,Vite配置了代理,将 `/api` 请求转发到后端服务器:
- 前端: http://localhost:5173
- 后端: http://localhost:8080

配置见 `vite.config.js`

## 注意事项
1. 确保后端服务已启动(默认端口8080)
2. Token会自动添加到请求头中
3. 401错误会自动跳转到登录页
4. 需要安装dayjs依赖: `npm install dayjs`

## 后续优化建议
1. 添加加载状态管理
2. 实现错误边界处理
3. 添加更多表单验证
4. 实现离线缓存
5. 添加单元测试
6. 优化首屏加载性能
