<template>
  <div class="container">
    <AppSidebar 
      :active-section="activeSection" 
      @navigate="handleNavigation"
      @logout="handleLogout"
    />
    
    <div class="main-content">
      <AppHeader @refresh="fetchMails" @compose="navigateTo('compose')" />
      
      <div class="content-area">
        <div class="section">
          <div class="section-header">
            <h2 class="section-title">邮件详情</h2>
            <div class="action-buttons">
              <button class="btn btn-outline" @click="replyMail">
                <i class="fas fa-reply"></i> 回复
              </button>
              <button class="btn btn-outline" @click="deleteMail">
                <i class="fas fa-trash-alt"></i> 删除
              </button>
            </div>
          </div>
          
          <div class="mail-detail">
            <div class="mail-header">
              <div class="mail-subject-detail">{{ mail.subject }}</div>
              <div class="mail-meta">
                <div>
                  <span class="mail-sender-detail">{{ mail.from }}</span>
                  <div class="mail-recipient">发送至: {{ mail.to }}</div>
                </div>
                <div>{{ formatDate(mail.timestamp) }}</div>
              </div>
            </div>
            
            <div class="mail-body" v-html="formatContent(mail.content)"></div>
            
            <div class="attachments" v-if="mail.attachmentPaths && mail.attachmentPaths.length > 0">
              <h3><i class="fas fa-paperclip"></i> 附件 ({{ mail.attachmentPaths.length }})</h3>
              <div class="attachment-list">
                <div 
                  v-for="(attachment, index) in mail.attachmentPaths" 
                  :key="index"
                  class="attachment-item"
                  @click="downloadAttachment(attachment)"
                >
                  <i class="fas" :class="getFileIcon(attachment)"></i>
                  <div class="attachment-name">{{ getFileName(attachment) }}</div>
                  <i class="fas fa-download"></i>
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
import { useRouter, useRoute } from 'vue-router'
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
    const route = useRoute()
    const mail = ref({
      id: null,
      from: '',
      to: '',
      subject: '',
      content: '',
      timestamp: '',
      attachmentPaths: []
    })
    const loading = ref(false)
    const activeSection = ref('inbox') // 根据邮件来源设置
    
    const user = ref({
      name: '测试用户',
      email: 'test@example.com'
    })
    
    const fetchMailDetail = async () => {
      try {
        loading.value = true
        const mailId = route.params.id
        if (!mailId) {
          throw new Error('无效的邮件ID')
        }
        
        const data = await emailApi.getMailDetail(mailId)
        mail.value = data
        
        // 标记为已读
        if (!data.read) {
          // 这里可以调用标记为已读的API
        }
        
      } catch (error) {
        console.error('获取邮件详情失败:', error)
        alert('获取邮件详情失败，请重试')
        router.go(-1) // 返回上一页
      } finally {
        loading.value = false
      }
    }
    
    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleString()
    }
    
    const formatContent = (content) => {
      // 简单处理换行，实际可以根据需要添加更多处理
      return content.replace(/\n/g, '<br>')
    }
    
    const getFileIcon = (filePath) => {
      const extension = filePath.split('.').pop().toLowerCase()
      const icons = {
        pdf: 'fa-file-pdf',
        doc: 'fa-file-word',
        docx: 'fa-file-word',
        xls: 'fa-file-excel',
        xlsx: 'fa-file-excel',
        ppt: 'fa-file-powerpoint',
        pptx: 'fa-file-powerpoint',
        jpg: 'fa-file-image',
        jpeg: 'fa-file-image',
        png: 'fa-file-image',
        gif: 'fa-file-image',
        zip: 'fa-file-archive',
        rar: 'fa-file-archive',
        txt: 'fa-file-alt',
        mp3: 'fa-file-audio',
        mp4: 'fa-file-video'
      }
      return icons[extension] || 'fa-file'
    }
    
    const getFileName = (filePath) => {
      return filePath.split('/').pop()
    }
    
    const downloadAttachment = async (filePath) => {
      try {
        const fileName = getFileName(filePath)
        const blob = await emailApi.downloadAttachment(fileName)
        
        // 创建下载链接
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = fileName
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        window.URL.revokeObjectURL(url)
        
      } catch (error) {
        console.error('下载附件失败:', error)
        alert('下载附件失败，请重试')
      }
    }
    
    const replyMail = () => {
      router.push({
        name: 'compose',
        query: {
          replyTo: mail.value.id,
          subject: `回复: ${mail.value.subject}`,
          to: mail.value.from
        }
      })
    }
    
    const deleteMail = async () => {
      if (confirm('确定要删除这封邮件吗？')) {
        try {
          // 调用删除邮件的API
          await emailApi.deleteMail(mail.value.id)
          
          // 删除成功后显示提示并跳转
          alert('邮件已删除')
          router.push({ name: 'inbox' })
        } catch (error) {
          console.error('删除邮件失败:', error)
          alert('删除邮件失败，请重试')
        }
      }
    }
    
    const handleNavigation = (section) => {
      activeSection.value = section
      router.push({ name: section })
    }
    
    const handleLogout = () => {
      router.push({ name: 'login' })
    }
    
    const navigateTo = (section) => {
      activeSection.value = section
      router.push({ name: section })
    }
    
    const fetchMails = () => {
      router.push({ name: 'inbox' })
    }
    
    onMounted(fetchMailDetail)
    
    return {
      mail,
      loading,
      activeSection,
      user,
      formatDate,
      formatContent,
      getFileIcon,
      getFileName,
      downloadAttachment,
      replyMail,
      deleteMail,
      handleNavigation,
      handleLogout,
      navigateTo,
      fetchMails
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

.mail-detail {
  background: white;
  border-radius: 8px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.mail-header {
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 20px;
}

.mail-subject-detail {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 10px;
}

.mail-meta {
  display: flex;
  justify-content: space-between;
  color: #6c757d;
}

.mail-sender-detail {
  font-weight: 500;
  color: var(--text);
}

.mail-recipient {
  margin-top: 5px;
  font-size: 0.9em;
  color: #6c757d;
}

.mail-body {
  line-height: 1.8;
  margin-bottom: 30px;
  white-space: pre-wrap;
}

.attachments {
  margin-top: 25px;
}

.attachments h3 {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.attachment-list {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.attachment-item {
  border: 1px solid var(--border);
  border-radius: 5px;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  width: 200px;
  cursor: pointer;
  transition: all 0.2s;
}

.attachment-item:hover {
  background-color: #f5f5f5;
}

.attachment-icon {
  font-size: 1.2rem;
}

.attachment-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 文件类型图标颜色 */
.fa-file-pdf { color: #f40; }
.fa-file-word { color: #2b579a; }
.fa-file-excel { color: #217346; }
.fa-file-powerpoint { color: #d24726; }
.fa-file-image { color: #87d068; }
.fa-file-archive { color: #fa8c16; }
.fa-file-audio { color: #13c2c2; }
.fa-file-video { color: #722ed1; }

.action-buttons {
  display: flex;
  gap: 15px;
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
