<template>
  <div class="container">
    <AppSidebar 
      :active-section="activeSection" 
      @navigate="handleNavigation"
      @logout="handleLogout"
    />
    
    <div class="main-content">
      <AppHeader @refresh="fetchTrash" @compose="navigateTo('compose')" />
      
      <div class="content-area">
        <div class="section">
          <div class="section-header">
            <h2 class="section-title">
              <i class="fas fa-trash-alt"></i> 回收站
            </h2>
          </div>
          
          <!-- 错误消息提示 -->
          <div v-if="error" class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ error }}
          </div>
          
          <div class="mail-list">
            <div 
              v-for="mail in trashMails" 
              :key="mail.id"
              class="mail-item"
            >
              <div class="mail-checkbox">
                <input type="checkbox" v-model="selectedMails" :value="mail.id">
              </div>
              <div class="mail-sender">{{ mail.from }}</div>
              <div class="mail-content">
                <div class="mail-subject">
                  {{ mail.subject }}
                  <i v-if="mail.attachmentPaths && mail.attachmentPaths.trim() !== ''" 
                     class="fas fa-paperclip attachment-indicator"></i>
                </div>
                <div class="mail-preview">{{ mail.content.substring(0, 50) }}...</div>
              </div>
              <div class="mail-time">{{ formatDate(mail.timestamp) }}</div>
           
            </div>
            
            <div v-if="loading" class="loading">
              <i class="fas fa-spinner fa-spin"></i> 加载中...
            </div>
            <div v-if="!loading && trashMails.length === 0" class="empty">
              <i class="fas fa-trash-alt empty-icon"></i>
              <p>回收站为空</p>
            </div>
          </div>
          
          <div class="trash-info">
            <i class="fas fa-info-circle"></i>
            <p>邮件在回收站中保留30天，之后将自动永久删除</p>
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
import emailApi from '@/api/email.api' // 导入API接口

export default {
  components: {
    AppHeader,
    AppSidebar
  },
  setup() {
    const router = useRouter()
    const trashMails = ref([])
    const loading = ref(false)
    const activeSection = ref('trash')
    const selectedMails = ref([])
    const error = ref(null)
    
    // 获取回收站数据
    const fetchTrash = async () => {
      loading.value = true
      error.value = null
      
      try {
        // 直接使用API返回的数据数组（http拦截器已处理）
        trashMails.value = await emailApi.getDeleted()
      } catch (err) {
        console.error('获取回收站邮件失败:', err)
        error.value = err.message || '获取回收站邮件失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    // 恢复单个邮件
    const restoreMail = async (mailId) => {
      const mail = trashMails.value.find(m => m.id === mailId)
      if (!mail) return
      
      if (confirm(`确定要恢复「${mail.subject}」邮件吗？`)) {
        try {
          loading.value = true
          await emailApi.restoreMail(mailId)
          await fetchTrash() // 刷新列表
        } catch (err) {
          console.error('恢复邮件失败:', err)
          error.value = err.message || '恢复邮件失败，请稍后重试'
        } finally {
          loading.value = false
        }
      }
    }
    
    // 删除单个邮件（永久删除）
    const deleteMail = async (mailId) => {
      const mail = trashMails.value.find(m => m.id === mailId)
      if (!mail) return
      
      if (confirm(`确定要永久删除「${mail.subject}」邮件吗？此操作不可撤销！`)) {
        try {
          loading.value = true
          await emailApi.deleteMail(mailId) // 使用已存在的删除接口
          await fetchTrash() // 刷新列表
        } catch (err) {
          console.error('永久删除邮件失败:', err)
          error.value = err.message || '永久删除邮件失败，请稍后重试'
        } finally {
          loading.value = false
        }
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
    
    onMounted(fetchTrash)
    
    return {
      trashMails,
      loading,
      activeSection,
      selectedMails,
      error,
      fetchTrash,
      formatDate,
      restoreMail,
      deleteMail,
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

.section-title i {
  color: var(--danger);
}

/* 邮件列表样式 */
.mail-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  position: relative;

}

.mail-item {
  padding: 15px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  transition: background 0.2s;
  background-color: #f9f9f9;
}

.mail-item:hover {
  background: #f0f0f0;
}

.mail-checkbox {
  margin-right: 15px;
  display: flex;
  align-items: center;
}

.mail-sender {
  width: 180px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mail-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.mail-subject {
  font-weight: 500;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #555;
}

.mail-preview {
  color: #777;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mail-time {
  width: 100px;
  text-align: right;
  color: #777;
  font-size: 0.85rem;
  margin: 0 15px;
}

.mail-actions {
  display: flex;
  gap: 10px;
}

.icon-btn {
  background: none;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #f0f0f0;
}

.restore-icon {
  color: var(--success);
  font-size: 16px;
}

.delete-icon {
  color: var(--danger);
  font-size: 18px;
}

/* 附件指示器 */
.attachment-indicator {
  color: var(--warning);
  margin-left: 5px;
}

/* 加载中和空状态 */
.loading, .empty {
  padding: 40px 20px;
  text-align: center;
  color: #777;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.loading i {
  font-size: 2rem;
  margin-bottom: 10px;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.empty-icon {
  font-size: 3rem;
  color: #ccc;
}

/* 回收站信息提示 */
.trash-info {
  background: #e9f7fe;
  border-radius: 8px;
  padding: 15px 20px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #0c5460;
  font-size: 0.9rem;
}

.trash-info i {
  font-size: 1.2rem;
  margin-top: 2px;
}

/* 错误提示 */
.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 10px 15px;
  border-radius: 5px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .mail-sender {
    width: 150px;
  }
  
  .mail-time {
    width: 80px;
  }
}

@media (max-width: 768px) {
  .mail-item {
    flex-wrap: wrap;
    padding: 12px 15px;
  }
  
  .mail-sender {
    width: 100%;
    margin-bottom: 5px;
  }
  
  .mail-content {
    flex: 0 0 calc(100% - 150px);
  }
  
  .mail-time {
    width: auto;
    margin-left: auto;
  }
  
  .mail-actions {
    margin-left: auto;
  }
}
</style>
