<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h2><i class="fas fa-envelope"></i> 邮件系统</h2>
        <p>安全、高效的邮件通信平台</p>
      </div>
      
      <div class="auth-tabs">
        <div 
          class="auth-tab" 
          :class="{ active: activeTab === 'login' }"
          @click="activeTab = 'login'"
        >
          登录
        </div>
        <div 
          class="auth-tab" 
          :class="{ active: activeTab === 'register' }"
          @click="activeTab = 'register'"
        >
          注册
        </div>
      </div>
      
      <!-- 登录表单 -->
      <form 
        v-show="activeTab === 'login'" 
        class="auth-form" 
        @submit.prevent="handleLogin"
      >
        <div class="form-group">
          <label for="loginEmail">邮箱地址</label>
          <input 
            type="email" 
            id="loginEmail" 
            v-model="loginForm.email"
            class="form-control" 
            placeholder="输入您的邮箱" 
            required
          >
        </div>
        <div class="form-group">
          <label for="loginPassword">密码</label>
          <input 
            type="password" 
            id="loginPassword" 
            v-model="loginForm.password"
            class="form-control" 
            placeholder="输入您的密码" 
            required
          >
        </div>
        <button 
          type="submit" 
          class="btn btn-primary" 
          style="width: 100%;"
          :disabled="loading"
        >
          <i class="fas fa-sign-in-alt"></i> 
          {{ loading ? '处理中...' : '登录' }}
        </button>
        <div class="form-footer">
          <a href="#" id="forgotPassword">忘记密码?</a>
        </div>
      </form>
      
      <!-- 注册表单 -->
      <form 
        v-show="activeTab === 'register'" 
        class="auth-form"
        @submit.prevent="handleRegister"
      >
        <div class="form-group">
          <label for="registerEmail">邮箱地址</label>
          <input 
            type="email" 
            id="registerEmail" 
            v-model="registerForm.email"
            class="form-control" 
            placeholder="输入您的邮箱" 
            required
          >
        </div>
        <div class="form-group">
          <label for="registerPassword">密码</label>
          <input 
            type="password" 
            id="registerPassword" 
            v-model="registerForm.password"
            class="form-control" 
            placeholder="设置您的密码" 
            required
          >
        </div>
        <div class="form-group">
          <label for="registerPasswordConfirm">确认密码</label>
          <input 
            type="password" 
            id="registerPasswordConfirm" 
            v-model="registerForm.confirmPassword"
            class="form-control" 
            placeholder="再次输入密码" 
            required
          >
        </div>
        <button 
          type="submit" 
          class="btn btn-primary" 
          style="width: 100%;"
          :disabled="loading"
        >
          <i class="fas fa-user-plus"></i> 
          {{ loading ? '处理中...' : '注册' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import emailApi from '@/api/email.api.js'
import websocket from '@/utils/websocket'

const router = useRouter()

// 状态管理
const activeTab = ref('login')
const loading = ref(false)
const loginForm = reactive({
  email: '',
  password: ''
})
const registerForm = reactive({
  email: '',
  password: '',
  confirmPassword: ''
})

// 登录处理
const handleLogin = async () => {
  if (loading.value) return
  loading.value = true
  
  try {
    const response = await emailApi.login(
      loginForm.email, 
      loginForm.password
    )
    
    // 存储 token 和用户信息
    localStorage.setItem('auth_token', response.token)
    localStorage.setItem('user_info', JSON.stringify(response.user))
    
    // 连接 WebSocket
    websocket.connect(loginForm.email)
    
    // 登录成功后跳转到首页
    router.push('/inbox')
    
  } catch (error) {
    console.error('登录失败:', error)
    alert(`登录失败: ${error.message || '请检查您的邮箱和密码'}`)
  } finally {
    loading.value = false
  }
}

// 注册处理
const handleRegister = async () => {
  if (loading.value) return
  
  // 密码确认验证
  if (registerForm.password !== registerForm.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  
  try {
    await emailApi.register(
      registerForm.email, 
      registerForm.password
    )
    
    // 注册成功后切换到登录表单
    activeTab.value = 'login'
    loginForm.email = registerForm.email
    loginForm.password = registerForm.password
    
    alert('注册成功！请登录')
    
  } catch (error) {
    console.error('注册失败:', error)
    alert(`注册失败: ${error.message || '请稍后重试'}`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 直接从原HTML中复制的样式 */
.auth-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #4361ee, #3f37c9);
}

.auth-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  width: 450px;
  padding: 40px;
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.auth-header h2 {
  color: #1d3557;
  margin-bottom: 10px;
}

.auth-header p {
  color: #6c757d;
}

.auth-tabs {
  display: flex;
  margin-bottom: 25px;
  border-bottom: 1px solid #dee2e6;
}

.auth-tab {
  flex: 1;
  text-align: center;
  padding: 12px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.auth-tab.active {
  border-bottom: 3px solid #4361ee;
  color: #4361ee;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #dee2e6;
  border-radius: 5px;
  font-size: 1rem;
}

.form-control:focus {
  outline: none;
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.15);
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn-primary {
  background: #4361ee;
  color: white;
}

.btn-primary:hover {
  background: #3f37c9;
}

.btn-primary:disabled {
  background: #a0a7e6;
  cursor: not-allowed;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  color: #6c757d;
}

.form-footer a {
  color: #4361ee;
  text-decoration: none;
  font-weight: 500;
}
</style>
