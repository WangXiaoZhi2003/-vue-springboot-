<template>
  <div class="container">
    <AppSidebar 
      :active-section="activeSection" 
      @navigate="handleNavigation"
      @logout="handleLogout"
    />
    
    <div class="main-content">
      <AppHeader @refresh="fetchDigestData" @compose="navigateTo('compose')" />
      
      <div class="content-area">
        <div class="section">
          <div class="section-header">
            <h2 class="section-title">📊 邮件摘要</h2>
            <!-- 已移除导出PDF按钮 -->
          </div>
          
          <div class="digest-container">
            <!-- 邮件统计卡片 -->
            <div class="stats-cards">
              <div class="stat-card">
                <div class="stat-icon inbox-icon">
                  <i class="fas fa-inbox"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.unreadCount }}</div>
                  <div class="stat-label">未读邮件</div>
                </div>
              </div>
              
              <div class="stat-card">
                <div class="stat-icon starred-icon">
                  <i class="fas fa-star"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.starredCount }}</div>
                  <div class="stat-label">星标邮件</div>
                </div>
              </div>
              
              <div class="stat-card">
                <div class="stat-icon sent-icon">
                  <i class="fas fa-paper-plane"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.sentCount }}</div>
                  <div class="stat-label">已发送</div>
                </div>
              </div>
              
              <div class="stat-card">
                <div class="stat-icon draft-icon">
                  <i class="fas fa-file-alt"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.draftCount }}</div>
                  <div class="stat-label">草稿</div>
                </div>
              </div>
            </div>
            
            <!-- 重要邮件摘要 -->
            <div class="digest-section">
              <h3 class="digest-section-title">
                <i class="fas fa-exclamation-circle"></i> 重要邮件摘要
              </h3>
              <div class="important-mails">
                <div 
                  v-for="mail in importantMails" 
                  :key="mail.id"
                  class="important-mail"
                  @click="viewMail(mail)"
                >
                  <div class="mail-header">
                    <div class="mail-sender">{{ mail.sender }}</div>
                    <div class="mail-time">{{ formatDate(mail.timestamp) }}</div>
                  </div>
                  <div class="mail-subject">{{ mail.subject }}</div>
                  <div class="mail-preview">{{ mail.preview }}</div>
                </div>
              </div>
            </div>
            
            <!-- 未读邮件摘要 -->
            <div class="digest-section">
              <h3 class="digest-section-title">
                <i class="fas fa-envelope"></i> 未读邮件摘要
              </h3>
              <div class="unread-mails">
                <div 
                  v-for="mail in unreadMails" 
                  :key="mail.id"
                  class="unread-mail"
                  @click="viewMail(mail)"
                >
                  <div class="mail-header">
                    <div class="mail-sender">{{ mail.sender }}</div>
                    <div class="mail-time">{{ formatDate(mail.timestamp) }}</div>
                  </div>
                  <div class="mail-subject">{{ mail.subject }}</div>
                </div>
              </div>
            </div>
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
import emailApi from '@/api/email.api' // 导入API模块

export default {
  components: {
    AppHeader,
    AppSidebar
  },
  setup() {
    const router = useRouter()
    const activeSection = ref('digest')
    const loading = ref(false)
    
    // 邮件统计数据 - 初始化为空值
    const stats = ref({
      unreadCount: 0,
      starredCount: 0,
      sentCount: 0,
      draftCount: 0
    })
    
    // 重要邮件 - 初始化为空数组
    const importantMails = ref([])
    
    // 未读邮件 - 初始化为空数组
    const unreadMails = ref([])
    
    // 获取摘要数据 - 从API获取
    const fetchDigestData = async () => {
      loading.value = true
      try {
        // 并行获取所有数据
        const [statsData, importantData, unreadData] = await Promise.all([
          emailApi.getMailStats(),
          emailApi.getImportantMails(),
          emailApi.getUnreadMails()
        ])
        
        // 更新状态
        stats.value = statsData
        importantMails.value = importantData
        unreadMails.value = unreadData
        
      } catch (error) {
        console.error('获取摘要数据失败:', error)
        // 这里可以添加错误处理，如显示提示信息
      } finally {
        loading.value = false
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        month: 'short',
        day: 'numeric'
      })
    }
    
    // 查看邮件
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
    
    onMounted(() => {
      // 组件挂载时自动获取数据
      fetchDigestData()
    })
    
    return {
      activeSection,
      loading,
      stats,
      importantMails,
      unreadMails,
      fetchDigestData,
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
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border);
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 摘要容器 */
.digest-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  display: flex;
  align-items: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 20px;
}

.inbox-icon {
  background: rgba(67, 97, 238, 0.15);
  color: #4361ee;
}

.starred-icon {
  background: rgba(255, 215, 0, 0.15);
  color: #ffd700;
}

.sent-icon {
  background: rgba(76, 201, 240, 0.15);
  color: #4cc9f0;
}

.draft-icon {
  background: rgba(108, 117, 125, 0.15);
  color: #6c757d;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  color: #6c757d;
  font-size: 1rem;
}

/* 摘要区域 */
.digest-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 25px;
}

.digest-section-title {
  font-size: 1.4rem;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 重要邮件 */
.important-mails {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.important-mail {
  padding: 18px;
  border-radius: 10px;
  background: #fff9f2;
  border-left: 4px solid #ff9f1c;
  cursor: pointer;
  transition: all 0.3s ease;
}

.important-mail:hover {
  background: #fff4e6;
  transform: translateX(5px);
}

.mail-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.mail-sender {
  font-weight: 600;
  color: #333;
}

.mail-time {
  color: #6c757d;
  font-size: 0.9rem;
}

.mail-subject {
  font-weight: 600;
  margin-bottom: 8px;
  color: #e8590c;
}

.mail-preview {
  color: #6c757d;
  font-size: 0.95rem;
  line-height: 1.5;
}

/* 未读邮件 */
.unread-mails {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.unread-mail {
  padding: 15px;
  border-radius: 8px;
  background: #f0f7ff;
  border-left: 3px solid #3d5af1;
  cursor: pointer;
  transition: all 0.3s ease;
}

.unread-mail:hover {
  background: #e6f0ff;
  transform: translateY(-3px);
}

/* 按钮样式 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
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
