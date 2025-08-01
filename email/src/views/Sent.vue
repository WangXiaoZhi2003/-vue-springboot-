<template>
    <div class="container">
      <AppSidebar 
 
        @navigate="handleNavigation"
        @logout="handleLogout"
      />
      
      <div class="main-content">
        <AppHeader @refresh="fetchSent" @compose="navigateTo('compose')" />
        
        <div class="content-area">
          <div class="section">
            <div class="section-header">
              <h2 class="section-title">已发送</h2>
              <div class="action-buttons">
                <button class="btn btn-outline archive-btn">
                  <i class="fas fa-archive"></i> 归档
                </button>
              </div>
            </div>
            
            <div class="mail-list">
              <div 
                v-for="mail in sentMails" 
                :key="mail.id"
                class="mail-item"
                @click="viewMail(mail)"
              >
                <div class="mail-checkbox">
                  <input type="checkbox">
                </div>
                <div class="mail-sender">我</div>
                <div class="mail-content">
                  <div class="mail-subject">
                    {{ mail.subject }}
                    <i v-if="mail.attachmentPaths" class="fas fa-paperclip attachment-indicator"></i>
                  </div>
                  <div class="mail-preview">{{ mail.content.substring(0, 50) }}...</div>
                </div>
                <div class="mail-time">{{ formatDate(mail.timestamp) }}</div>
              </div>
              
              <div v-if="loading" class="loading">加载中...</div>
              <div v-if="!loading && sentMails.length === 0" class="empty">没有已发送的邮件</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import emailApi from '@/api/email.api'
  import AppHeader from '@/components/layout/Header.vue'
  import AppSidebar from '@/components/layout/Sidebar.vue'
  
  export default {
    components: {
      AppHeader,
      AppSidebar
    },
    setup() {
      const router = useRouter()
      const sentMails = ref([])
      const loading = ref(false)
      const activeSection = ref('sent')
      
      const user = ref({
        name: '测试用户',
        email: 'test@example.com'
      })
      
      const fetchSent = async () => {
        try {
          loading.value = true
          sentMails.value = await emailApi.getSent()
        } catch (error) {
          console.error('获取已发送邮件失败:', error)
        } finally {
          loading.value = false
        }
      }
      
      const formatDate = (dateString) => {
        const date = new Date(dateString)
        return date.toLocaleString()
      }
      
      const viewMail = (mail) => {
        router.push({ name: 'mail-detail', params: { id: mail.id } })
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
      
      onMounted(fetchSent)
      
      return {
        sentMails,
        loading,
        activeSection,
        user,
        fetchSent,
        formatDate,
        viewMail,
        handleNavigation,
        handleLogout,
        navigateTo
      }
    }
  }
  </script>
  
  <style scoped>
  /* 复用与收件箱相同的样式 */
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
  }
  
  .archive-btn {
    padding: 8px 15px !important;
    font-size: 0.9rem !important;
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
  
  .mail-item:hover {
    background: #f8f9fa;
  }
  
  .mail-checkbox {
    margin-right: 15px;
    display: flex;
    align-items: center;
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
  
  .btn {
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.2s;
  }
  
  .btn-outline {
    background: transparent;
    border: 1px solid var(--primary);
    color: var(--primary);
  }
  
  .btn-outline:hover {
    background: var(--primary);
    color: white;
  }
  </style>
  