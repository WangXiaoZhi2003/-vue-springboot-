<template>
  <div class="container">
    <AppSidebar 
      :active-section="activeSection" 
      @navigate="handleNavigation"
      @logout="handleLogout"
    />
    
    <div class="main-content">
      <AppHeader @refresh="fetchSpam" @compose="navigateTo('compose')" />
      
      <div class="content-area">
        <div class="section">
          <div class="section-header">
            <h2 class="section-title">🗑️ 垃圾邮件</h2>
          </div>
          
          <div class="mail-list">
            <div 
              v-for="mail in spamMails" 
              :key="mail.id"
              class="mail-item spam-item"
              :class="{ unread: mail.read === 0 }"
              @click="viewMailDetail(mail.id)"
            >
              <div class="mail-sender">{{ mail.from }}</div>
              <div class="mail-content">
                <div class="mail-subject">
                  {{ mail.subject }}
                  <i v-if="mail.attachmentPaths && mail.attachmentPaths !== ''" 
                     class="fas fa-paperclip attachment-indicator"></i>
                </div>
                <div class="mail-preview">{{ mail.content.substring(0, 50) }}...</div>
              </div>
              <div class="mail-time">{{ formatDate(mail.timestamp) }}</div>
            </div>
            
            <div v-if="loading" class="loading">加载中...</div>
            <div v-if="!loading && spamMails.length === 0" class="empty">垃圾邮件箱为空</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/Header.vue'
import AppSidebar from '@/components/layout/Sidebar.vue'
import emailApi from '@/api/email.api'

export default {
  components: {
    AppHeader,
    AppSidebar
  },
  setup() {
    const router = useRouter()
    const spamMails = ref([])
    const loading = ref(false)
    const activeSection = ref('spam')
    
    // 获取垃圾邮件数据
    const fetchSpam = async () => {
      try {
        loading.value = true
        const response = await emailApi.getSpam()
        spamMails.value = response || []
      } catch (error) {
        console.error('获取垃圾邮件失败:', error)
        alert('获取垃圾邮件失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
    
    // 查看邮件详情并标记为已读
    const viewMailDetail = async (mailId) => {
      try {
        // 如果邮件未读，先标记为已读
        const mail = spamMails.value.find(m => m.id === mailId)
        if (mail && mail.read === 0) {
          await emailApi.markAsRead(mailId)
          // 更新本地状态
          mail.read = 1
        }
        
        // 跳转到邮件详情页
        router.push({ name: 'mail-detail', params: { id: mailId } })
      } catch (error) {
        console.error('标记已读失败:', error)
        // 即使标记失败也允许查看邮件
        router.push({ name: 'mail-detail', params: { id: mailId } })
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      try {
        const date = new Date(dateString)
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (e) {
        return dateString // 如果解析失败，返回原始字符串
      }
    }
    
    const handleNavigation = (section) => {
      activeSection.value = section
      router.push({ name: section })
    }
    
    const handleLogout = () => {
      router.push({ name: 'auth' })
    }
    
    const navigateTo = (section) => {
      activeSection.value = section
      router.push({ name: section })
    }
    
    onMounted(fetchSpam)
    
    return {
      spamMails,
      loading,
      activeSection,
      fetchSpam,
      viewMailDetail,
      formatDate,
      handleNavigation,
      handleLogout,
      navigateTo
    }
  }
}
</script>

<style scoped>
.container {
  display: flex;
  min-height: 100vh;
  max-width: 100%;
  margin: 0 auto;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  background: white;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-area {
  flex: 1;
  padding: 20px 30px;
  overflow-y: auto;
  background: var(--light);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border);
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.mail-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.mail-item {
  padding: 15px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  cursor: pointer;
  transition: background 0.2s;
}

.spam-item {
  opacity: 0.8;
  background-color: #fff9f9;
}

.spam-item:hover {
  opacity: 1;
  background-color: #fff0f0;
}

.mail-item.unread {
  background: #f0f7ff;
  font-weight: 600;
}

.mail-sender {
  width: 200px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
}

.mail-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  justify-content: center;
}

.mail-subject {
  font-weight: 500;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mail-preview {
  color: #6c757d;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mail-time {
  width: 100px;
  text-align: right;
  color: #6c757d;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
}

.attachment-indicator {
  color: var(--warning);
  margin-left: 5px;
}

.loading, .empty {
  padding: 20px;
  text-align: center;
  color: #6c757d;
}
</style>
