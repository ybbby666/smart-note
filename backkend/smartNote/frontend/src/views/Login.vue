<template>
  <div class="login-container flex items-center justify-center min-h-screen bg-gray-100">
    <el-card class="w-full max-w-md shadow-lg">
      <div class="text-center mb-8">
        <h2 class="text-3xl font-bold text-primary">SmartNote</h2>
        <p class="text-gray-500 mt-2">智能笔记，随手记录</p>
      </div>

      <!-- 登录方式切换 -->
      <el-tabs v-model="loginType" class="mb-6">
        <el-tab-pane label="密码登录" name="password"></el-tab-pane>
        <el-tab-pane label="验证码登录" name="verification"></el-tab-pane>
      </el-tabs>

      <!-- 密码登录表单 -->
      <el-form v-if="loginType === 'password'" :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top">
        <el-form-item label="账号" prop="account">
          <el-input v-model="passwordForm.account" placeholder="请输入用户名/邮箱/手机号" prefix-icon="User" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="passwordForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            show-password
            @keyup.enter="handlePasswordLogin"
          />
        </el-form-item>

        <div class="flex items-center justify-between mb-6">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link type="primary" underline="never">忘记密码?</el-link>
        </div>

        <el-button type="primary" class="w-full h-12 text-lg" :loading="loading" @click="handlePasswordLogin">
          登 录
        </el-button>
      </el-form>

      <!-- 验证码登录表单 -->
      <el-form v-else :model="emailForm" :rules="emailRules" ref="emailFormRef" label-position="top">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="emailForm.email" placeholder="请输入邮箱地址" prefix-icon="Message" />
        </el-form-item>
        
        <el-form-item label="验证码" prop="code">
          <div class="flex gap-2">
            <el-input 
              v-model="emailForm.code" 
              placeholder="请输入验证码" 
              prefix-icon="Key"
              maxlength="6"
              @keyup.enter="handleEmailLogin"
            />
            <el-button 
              :disabled="countdown > 0" 
              @click="handleSendCode"
              :loading="sendingCode"
            >
              {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-button type="primary" class="w-full h-12 text-lg" :loading="loading" @click="handleEmailLogin">
          登 录
        </el-button>
      </el-form>

      <div class="text-center mt-6">
        <span class="text-gray-500 text-sm">还没有账号?</span>
        <el-link type="primary" class="text-sm ml-1" underline="never" @click="handleRegister">立即注册</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, emailLogin, getMyProfile, sendVerificationCode } from '@/api/user'

const router = useRouter()
const passwordFormRef = ref(null)
const emailFormRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const rememberMe = ref(false)
const loginType = ref('password')
const countdown = ref(0)
let timer = null

// 密码登录表单
const passwordForm = reactive({
  account: '',
  password: ''
})

// 邮箱验证码登录表单
const emailForm = reactive({
  email: '',
  code: ''
})

const passwordRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 发送验证码
const handleSendCode = async () => {
  if (!emailFormRef.value) return
  
  try {
    await emailFormRef.value.validateField('email')
  } catch (error) {
    return
  }
  
  sendingCode.value = true
  try {
    await sendVerificationCode({
      email: emailForm.email,
      type: 'login'
    })
    ElMessage.success('验证码已发送，请查收邮箱')
    // 开始倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败', error)
  } finally {
    sendingCode.value = false
  }
}

// 密码登录
const handlePasswordLogin = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const data = await login(passwordForm)
        // 后端返回的是 { accessToken, refreshToken }
        localStorage.setItem('token', data.accessToken)
        localStorage.setItem('refreshToken', data.refreshToken)
        ElMessage.success('登录成功')
        // 获取用户信息后再跳转
        try {
          const userInfo = await getMyProfile()
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
        } catch (error) {
          console.error('获取用户信息失败', error)
        }
        router.push('/')
      } catch (error) {
        console.error('登录失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 邮箱验证码登录
const handleEmailLogin = async () => {
  if (!emailFormRef.value) return
  
  await emailFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const data = await emailLogin(emailForm)
        // 后端返回的是 { accessToken, refreshToken }
        localStorage.setItem('token', data.accessToken)
        localStorage.setItem('refreshToken', data.refreshToken)
        ElMessage.success('登录成功')
        // 获取用户信息后再跳转
        try {
          const userInfo = await getMyProfile()
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
        } catch (error) {
          console.error('获取用户信息失败', error)
        }
        router.push('/')
      } catch (error) {
        console.error('登录失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
</style>
