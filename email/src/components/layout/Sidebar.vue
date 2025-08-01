<template>
  <div class="sidebar">
    <div class="logo">
      <h1><i class="fas fa-mail-bulk"></i> <span>邮件系统</span></h1>
    </div>

    <div class="nav-links">
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'inbox' }" 
        @click="navigate('inbox')"
      >
        <i class="fas fa-inbox"></i> <span>收件箱</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'sent' }" 
        @click="navigate('sent')"
      >
        <i class="fas fa-paper-plane"></i> <span>已发送</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'compose' }" 
        @click="navigate('compose')"
      >
        <i class="fas fa-edit"></i> <span>写邮件</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'starred' }" 
        @click="navigate('starred')"
      >
        <i class="fas fa-star"></i> <span>星标邮件</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'drafts' }" 
        @click="navigate('drafts')"
      >
        <i class="fas fa-file-alt"></i> <span>草稿箱</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'spam' }" 
        @click="navigate('spam')"
      >
        <i class="fas fa-ban"></i> <span>垃圾邮件</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'digest' }" 
        @click="navigate('digest')"
      >
        <i class="fas fa-newspaper"></i> <span>邮件摘要</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'trash' }" 
        @click="navigate('trash')"
      >
        <i class="fas fa-trash-alt"></i> <span>回收站</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'search' }" 
        @click="navigate('search')"
      >
        <i class="fas fa-search"></i> <span>搜索邮件</span>
      </div>
      <div 
        class="nav-item" 
        :class="{ active: currentSection === 'settings' }" 
        @click="navigate('settings')"
      >
        <i class="fas fa-cog"></i> <span>设置</span>
      </div>
    </div>

    <div class="user-info">
      <div class="user-avatar">{{ userInitial }}</div>
      <div class="user-details">
        <div class="name">{{ userName }}</div>
        <div class="email">{{ userEmail }}</div>
      </div>
      <button class="logout-btn" @click="handleLogout">
        <i class="fas fa-sign-out-alt"></i>
      </button>
    </div>
  </div>
</template>

<script>
import { useRouter, useRoute } from 'vue-router'
import websocket from '@/utils/websocket'
import { computed, ref, watch } from 'vue'

export default {
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // 当前活动部分 - 初始化为路由名称
    const currentSection = ref(route.name || 'inbox')
    
    // 监听路由变化更新活动部分
    watch(() => route.name, (newName) => {
      currentSection.value = newName
    })
    
    // 从 localStorage 获取用户信息
    const userInfo = computed(() => {
      const userData = localStorage.getItem('user_info')
      return userData ? JSON.parse(userData) : null
    })
    
    // 处理用户名 - 使用邮箱前缀
    const userName = computed(() => {
      if (!userInfo.value || !userInfo.value.username) return '用户名称'
      
      // 提取邮箱前缀（@之前的部分）
      const email = userInfo.value.username
      const atIndex = email.indexOf('@')
      return atIndex !== -1 ? email.substring(0, atIndex) : email
    })
    
    // 用户邮箱
    const userEmail = computed(() => {
      return userInfo.value?.username || 'user@example.com'
    })
    
    // 用户头像首字母
    const userInitial = computed(() => {
      return userName.value.charAt(0).toUpperCase()
    })
    
    // 导航处理
    const navigate = (section) => {
      // 更新当前活动部分
      currentSection.value = section
      
      // 路由跳转
      router.push({ name: section })
    }
    
    // 退出登录处理
    const handleLogout = () => {
      // 1. 关闭WebSocket连接
      websocket.disconnect()
      
      // 2. 移除认证token和用户信息
      localStorage.removeItem('auth_token')
      localStorage.removeItem('user_info')
      
      // 3. 跳转到登录页
      router.push('/login')
    }
    
    return {
      currentSection,
      userInfo,
      userName,
      userEmail,
      userInitial,
      navigate,
      handleLogout
    }
  }
}
</script>

<style scoped>
/* 保持原有的样式不变 */
.sidebar {
  width: 280px;
  background: var(--dark);
  color: white;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 0 20px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 20px;
}

.logo h1 {
  font-size: 1.8rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo i {
  color: var(--success);
}

.nav-links {
  flex: 1;
}

.nav-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item:hover,
.nav-item.active {
  background: rgba(255, 255, 255, 0.1);
  border-left: 4px solid var(--success);
}

.nav-item i {
  width: 24px;
  text-align: center;
}

.user-info {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
}

.user-details {
  flex: 1;
}

.user-details .name {
  font-weight: 600;
}

.user-details .email {
  font-size: 0.85rem;
  opacity: 0.8;
}

.logout-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 1.2rem;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .sidebar {
    width: 80px;
  }

  .logo h1 span,
  .nav-item span,
  .user-details {
    display: none;
  }

  .logo {
    text-align: center;
    padding: 20px 0;
  }

  .user-info {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    padding: 10px;
  }

  .logo {
    padding: 10px;
    text-align: left;
  }

  .nav-links {
    display: flex;
    overflow-x: auto;
  }

  .nav-item {
    padding: 10px 15px;
    white-space: nowrap;
  }

  .user-info {
    display: none;
  }
}
</style>
