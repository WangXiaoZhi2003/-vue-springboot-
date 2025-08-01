<template>
    <div class="container">
      <AppSidebar 
        :active-section="activeSection" 
        @navigate="handleNavigation"
        @logout="handleLogout"
      />
      
      <div class="main-content">
        <AppHeader @refresh="fetchStarred" @compose="navigateTo('compose')" />
        
        <div class="content-area">
          <div class="section">
            <div class="section-header">
              <h2 class="section-title">⭐ 星标邮件</h2>
              <div class="action-buttons">
                <button class="btn btn-outline archive-btn">
                  <i class="fas fa-archive"></i> 归档
                </button>
              </div>
            </div>
            
            <div class="mail-list">
              <div 
                v-for="mail in starredMails" 
                :key="mail.id"
                class="mail-item"
                :class="{ unread: !mail.read }"
                @click="viewMail(mail)"
                @mousedown="startPressTimer(mail)"
                @mouseup="clearPressTimer"
                @mouseleave="clearPressTimer"
                @touchstart="startPressTimer(mail)"
                @touchend="clearPressTimer"
                @touchcancel="clearPressTimer"
              >
                <div class="mail-checkbox">
                  <input type="checkbox">
                </div>
                <div class="mail-sender">{{ mail.from }}</div>
                <div class="mail-content">
                  <div class="mail-subject">
                    <!-- 星标图标 -->
                    <span class="star-icon"></span>
                    {{ mail.subject }}
                    <i v-if="mail.attachmentPaths" class="fas fa-paperclip attachment-indicator"></i>
                  </div>
                  <div class="mail-preview">{{ mail.content.substring(0, 50) }}...</div>
                </div>
                <div class="mail-time">{{ formatDate(mail.timestamp) }}</div>
              </div>
              
              <div v-if="loading" class="loading">加载中...</div>
              <div v-if="!loading && starredMails.length === 0" class="empty">没有星标邮件</div>
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
      const starredMails = ref([])
      const loading = ref(false)
      const activeSection = ref('starred')
      const pressTimer = ref(null)
      const pressedMail = ref(null)
      
      const fetchStarred = async () => {
        try {
          loading.value = true
          // 复用收件箱接口
          const inboxMails = await emailApi.getInbox()
          // 过滤出带星标的邮件
          starredMails.value = inboxMails.filter(mail => mail.starred)
        } catch (error) {
          console.error('获取星标邮件失败:', error)
        } finally {
          loading.value = false
        }
      }
      
      const formatDate = (dateString) => {
        const date = new Date(dateString)
        return date.toLocaleString()
      }
      
      const viewMail = async (mail) => {
        try {
          // 如果是未读邮件，先标记为已读
          if (!mail.read) {
            await emailApi.markAsRead(mail.id)
            // 更新本地邮件状态
            const mailIndex = starredMails.value.findIndex(m => m.id === mail.id)
            if (mailIndex !== -1) {
              starredMails.value[mailIndex].read = true
            }
          }
          // 跳转到邮件详情页
          router.push({ name: 'mail-detail', params: { id: mail.id } })
        } catch (error) {
          console.error('标记邮件为已读失败:', error)
          // 即使标记失败也继续跳转
          router.push({ name: 'mail-detail', params: { id: mail.id } })
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
      
      // 开始长按计时器
      const startPressTimer = (mail) => {
        pressedMail.value = mail
        pressTimer.value = setTimeout(() => {
          showStarConfirmation(mail)
        }, 1000) // 1秒长按触发
      }
      
      // 清除长按计时器
      const clearPressTimer = () => {
        if (pressTimer.value) {
          clearTimeout(pressTimer.value)
          pressTimer.value = null
          pressedMail.value = null
        }
      }
      
      // 显示标星确认对话框
      const showStarConfirmation = (mail) => {
        const action = mail.starred ? '取消标星' : '标星'
        const message = mail.starred 
          ? `确定要将「${mail.subject}」取消标星吗？` 
          : `确定要将「${mail.subject}」标记为星标邮件吗？`
        
        if (confirm(message)) {
          toggleStar(mail)
        }
        clearPressTimer()
      }
      
      // 切换邮件星标状态
      const toggleStar = async (mail) => {
        try {
          await emailApi.toggleStar(mail.id)
          // 更新本地邮件状态
          const mailIndex = starredMails.value.findIndex(m => m.id === mail.id)
          if (mailIndex !== -1) {
            starredMails.value[mailIndex].starred = !starredMails.value[mailIndex].starred
            // 如果取消星标，从列表中移除
            if (!starredMails.value[mailIndex].starred) {
              starredMails.value.splice(mailIndex, 1)
            }
          }
        } catch (error) {
          console.error('切换星标状态失败:', error)
          alert(`操作失败: ${error.message || '请稍后重试'}`)
        }
      }
      
      onMounted(fetchStarred)
      
      return {
        starredMails,
        loading,
        activeSection,
        fetchStarred,
        formatDate,
        viewMail,
        handleNavigation,
        handleLogout,
        navigateTo,
        startPressTimer,
        clearPressTimer,
        toggleStar
      }
    }
  }
  </script>
  
  <style scoped>
  /* 容器布局 */
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
  
  /* 分区头部样式 */
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
  
  /* 归档按钮样式修正 */
  .archive-btn {
    padding: 8px 15px !important;
    font-size: 0.9rem !important;
  }
  
  /* 邮件列表样式 */
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
    position: relative;
  }
  
  .mail-item:hover {
    background: #f8f9fa;
  }
  
  .mail-item.unread {
    background: #f0f7ff;
    font-weight: 600;
  }
  
  /* 邮件项各部分样式 */
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
    display: flex;
    align-items: center;
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
  
  /* 附件指示器 */
  .attachment-indicator {
    color: var(--warning);
    margin-left: 5px;
  }
  
  /* 加载中和空状态 */
  .loading, .empty {
    padding: 20px;
    text-align: center;
    color: #6c757d;
  }
  
  /* 按钮基础样式 */
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
  
  /* 完美对称的星标样式 */
  .star-icon {
    display: inline-block;
    position: relative;
    width: 16px;
    height: 16px;
    margin-right: 8px;
    background-color: #FFD700;
    clip-path: polygon(
      50% 0%,
      61% 35%,
      98% 35%,
      68% 57%,
      79% 91%,
      50% 70%,
      21% 91%,
      32% 57%,
      2% 35%,
      39% 35%
    );
    transform: scale(1.2);
  }
  </style>
  