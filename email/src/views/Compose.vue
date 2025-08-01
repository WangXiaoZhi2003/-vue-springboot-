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
            <h2 class="section-title">写邮件</h2>
            <div class="action-buttons">
              <button 
                class="btn btn-outline"
                @click="discardDraft"
              >
                <i class="fas fa-trash-alt"></i> 丢弃
              </button>
            </div>
          </div>
          
          <div class="mail-form">
            <!-- 草稿加载状态提示 -->
            <div v-if="loadingDraft" class="loading-draft">
              <i class="fas fa-spinner fa-spin"></i> 加载草稿中...
            </div>
            <div v-if="draftLoadError" class="error-draft">
              <i class="fas fa-exclamation-triangle"></i> 草稿加载失败: {{ draftLoadError }}
            </div>
            
            <div class="form-group">
              <label for="recipient">收件人</label>
              <input 
                type="email" 
                id="recipient" 
                v-model="form.receiverEmail"
                class="form-control" 
                placeholder="输入收件人邮箱" 
                required
              >
            </div>
            
            <div class="form-group">
              <label for="subject">主题</label>
              <input 
                type="text" 
                id="subject" 
                v-model="form.subject"
                class="form-control" 
                placeholder="邮件主题" 
                required
              >
            </div>
            
            <div class="form-group">
              <label for="content">内容</label>
              <textarea 
                id="content" 
                v-model="form.content"
                class="form-control" 
                placeholder="输入邮件内容..."
                rows="10"
              ></textarea>
            </div>
            
            <div class="form-group">
              <label for="attachment">附件</label>
              <input 
                type="file" 
                id="attachment" 
                ref="fileInput"
                multiple
                @change="handleAttachmentChange"
              >
              <div class="attachment-preview" v-if="attachments.length > 0">
                <div 
                  v-for="(file, index) in attachments" 
                  :key="file.name + index"
                  class="attachment-preview-item"
                >
                  <i class="fas" :class="getFileIcon(file)"></i>
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">({{ formatFileSize(file.size) }})</span>
                  <i 
                    class="fas fa-times remove-attachment"
                    @click="removeAttachment(index)"
                  ></i>
                  <div v-if="file.uploadProgress > 0" class="upload-progress">
                    <div 
                      class="progress-bar" 
                      :style="{ width: file.uploadProgress + '%' }"
                    ></div>
                    <span>{{ file.uploadProgress }}%</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="action-buttons" style="margin-top: 20px;">
              <button 
                class="btn btn-primary"
                @click="sendMail"
                :disabled="sending || isUploading"
              >
                <i class="fas fa-paper-plane"></i> 
                {{ sending ? '发送中...' : '发送' }}
              </button>
              <button 
                class="btn btn-outline"
                @click="saveDraft"
                :disabled="saving"
              >
                <i class="fas fa-save"></i> 
                {{ saving ? '保存中...' : '保存草稿' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
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
    const route = useRoute() // 获取当前路由信息
    const activeSection = ref('compose')
    const sending = ref(false)
    const saving = ref(false)
    const fileInput = ref(null)
    const loadingDraft = ref(false) // 草稿加载状态
    const draftLoadError = ref(null) // 草稿加载错误信息
    const currentDraftId = ref(null) // 当前编辑的草稿ID
    
    // 自动保存相关状态
    const autoSaveEnabled = ref(false)
    const autoSaveTimer = ref(null)
    const autoSaveInterval = 5 * 60 * 1000 // 5分钟
    
    const form = ref({
      receiverEmail: '',
      subject: '',
      content: ''
    })
    
    const attachments = ref([])
    const user = ref({
      name: '1测试用户',
      email: 'test@example.com'
    })

    const isUploading = computed(() => {
      return attachments.value.some(file => 
        file.uploadProgress > 0 && file.uploadProgress < 100
      )
    })

    // 添加附件时检查并暂停自动保存
    const hasAttachments = computed(() => attachments.value.length > 0)

    const getFileIcon = (file) => {
      const extension = file.name.split('.').pop().toLowerCase()
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
    
    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    // 新增：通过API加载草稿数据
    const loadDraft = async (draftId) => {
      if (!draftId) return
      
      loadingDraft.value = true
      draftLoadError.value = null
      currentDraftId.value = draftId
      
      try {
        const draftDetail = await emailApi.getMailDetail(draftId)
        
        // 填充表单
        form.value = {
          receiverEmail: draftDetail.to || '',
          subject: draftDetail.subject || '',
          content: draftDetail.content || ''
        }
        
        console.log('草稿加载成功:', draftDetail)
      } catch (error) {
        console.error('草稿加载失败:', error)
        draftLoadError.value = error.message || '无法加载草稿详情'
      } finally {
        loadingDraft.value = false
      }
    }
    
    const handleAttachmentChange = async (event) => {
      const files = Array.from(event.target.files)
      
      // 添加到附件列表
      attachments.value = [
        ...attachments.value,
        ...files.map(file => ({
          file,
          name: file.name,
          size: file.size,
          uploadProgress: 0,
          uploaded: false,
          path: null
        }))
      ]
      
      // 有附件时暂停自动保存计时器
      if (autoSaveEnabled.value && autoSaveTimer.value) {
        clearInterval(autoSaveTimer.value)
        autoSaveTimer.value = null
        console.log('检测到附件，自动保存已暂停')
      }
      
      // 自动开始上传
      await uploadAttachments(files)
    }
    
    const uploadAttachments = async (files) => {
      try {
        // 模拟上传进度
        files.forEach(file => {
          const interval = setInterval(() => {
            const attachment = attachments.value.find(a => a.name === file.name)
            if (attachment) {
              attachment.uploadProgress += 10
              if (attachment.uploadProgress >= 100) {
                attachment.uploadProgress = 100
                attachment.uploaded = true
                clearInterval(interval)
              }
            }
          }, 300)
        })

        // 实际API调用
        const response = await emailApi.uploadAttachment(files)
        
        // 更新附件状态
        attachments.value = attachments.value.map(attachment => {
          if (files.some(f => f.name === attachment.name)) {
            return {
              ...attachment,
              uploaded: true,
              path: response.path || response.url,
              uploadProgress: 100
            }
          }
          return attachment
        })
        
      } catch (error) {
        console.error('附件上传失败:', error)
        alert('部分附件上传失败，请重试')
        // 标记失败的附件
        attachments.value = attachments.value.map(attachment => {
          if (files.some(f => f.name === attachment.name)) {
            return {
              ...attachment,
              uploadProgress: 0,
              uploadError: true
            }
          }
          return attachment
        })
      }
    }
    
    const removeAttachment = (index) => {
      attachments.value.splice(index, 1)
      
      // 删除所有附件后恢复自动保存
      if (attachments.value.length === 0 && autoSaveEnabled.value) {
        startAutoSaveTimer()
        console.log('附件已全部删除，自动保存已恢复')
      }
    }
    
    // 启动自动保存计时器
    const startAutoSaveTimer = () => {
      if (autoSaveTimer.value) {
        clearInterval(autoSaveTimer.value)
      }
      autoSaveTimer.value = setInterval(autoSaveDraft, autoSaveInterval)
    }
    
    const sendMail = async () => {
      if (sending.value || isUploading.value) return
      
      // 检查必填字段
      if (!form.value.receiverEmail || !form.value.subject) {
        alert('请填写收件人和主题')
        return
      }
      
      // 检查是否有未上传完成的附件
      const hasUnuploaded = attachments.value.some(a => !a.uploaded && a.uploadProgress > 0)
      if (hasUnuploaded) {
        alert('请等待附件上传完成')
        return
      }
      
      sending.value = true
      
      try {
        const attachmentPaths = attachments.value
          .filter(a => a.uploaded)
          .map(a => a.path)
        
        const response = await emailApi.sendMail(
          form.value.receiverEmail,
          form.value.subject,
          form.value.content,
          attachmentPaths.length > 0 ? attachmentPaths : undefined
        )
        
        alert('邮件发送成功！')
        resetForm()
        router.push({ name: 'sent' })
        
      } catch (error) {
        console.error('邮件发送失败:', error)
        alert(`邮件发送失败: ${error.message || '请稍后重试'}`)
      } finally {
        sending.value = false
      }
    }
    
    // 保存草稿（手动和自动共用）
    const saveDraftHandler = async () => {
      // 检查是否有未上传完成的附件
      const hasUnuploaded = attachments.value.some(a => !a.uploaded && a.uploadProgress > 0)
      if (hasUnuploaded) {
        console.log('有附件正在上传，跳过草稿保存')
        return
      }
      
      // 收集已上传的附件路径
      const attachmentPaths = attachments.value
        .filter(a => a.uploaded)
        .map(a => a.path)
      
      try {
        // 如果有草稿ID，则更新草稿，否则创建新草稿
        if (currentDraftId.value) {
          await emailApi.updateDraft(
            currentDraftId.value,
            form.value.receiverEmail,
            form.value.subject,
            form.value.content,
            attachmentPaths.length > 0 ? attachmentPaths : undefined
          )
        } else {
          await emailApi.senddraftMail(
            form.value.receiverEmail,
            form.value.subject,
            form.value.content,
            attachmentPaths.length > 0 ? attachmentPaths : undefined
          )
        }
        
        return true
      } catch (error) {
        console.error('草稿保存失败:', error)
        return false
      }
    }
    
    // 手动保存草稿
    const saveDraft = async () => {
      if (saving.value) return
      
      // 检查是否有附件
      if (attachments.value.length > 0) {
        alert('保存草稿不支持携带附件文件，请删除后重试')
        return
      }
      
      saving.value = true
      try {
        const success = await saveDraftHandler()
        if (success) {
          alert('草稿保存成功')
        } else {
          alert('草稿保存失败，请稍后重试')
        }
      } catch (error) {
        console.error('保存草稿出错:', error)
        alert('保存草稿时出错')
      } finally {
        saving.value = false
      }
    }
    
    // 自动保存草稿
    const autoSaveDraft = async () => {
      if (!autoSaveEnabled.value) return
      if (saving.value) return
      
      // 如果有附件则跳过自动保存
      if (attachments.value.length > 0) {
        console.log('检测到附件，跳过自动保存')
        return
      }
      
      // 检查表单是否有内容
      const hasContent = form.value.receiverEmail || form.value.subject || form.value.content
      if (!hasContent) {
        console.log('表单无内容，跳过自动保存')
        return
      }
      
      console.log('自动保存草稿...')
      await saveDraftHandler()
    }
    
    // 初始化自动保存设置
    const initAutoSave = () => {
      // 从localStorage获取设置
      const savedSetting = localStorage.getItem('autoSaveDraftEnabled')
      autoSaveEnabled.value = savedSetting === 'true'
      
      // 如果开启且没有附件，则启动定时器
      if (autoSaveEnabled.value && attachments.value.length === 0) {
        startAutoSaveTimer()
        console.log('自动保存草稿功能已启用')
      }
    }
    
    const discardDraft = () => {
      if (confirm('确定要丢弃当前邮件吗？所有未保存的内容将会丢失。')) {
        resetForm()
        router.push({ name: 'inbox' })
      }
    }
    
    const resetForm = () => {
      form.value = {
        receiverEmail: '',
        subject: '',
        content: ''
      }
      attachments.value = []
      currentDraftId.value = null
      if (fileInput.value) {
        fileInput.value.value = ''
      }
      
      // 重置后恢复自动保存
      if (autoSaveEnabled.value) {
        startAutoSaveTimer()
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
    
    // 监听localStorage变化
    const handleStorageChange = (event) => {
      if (event.key === 'autoSaveDraftEnabled') {
        const newValue = event.newValue === 'true'
        
        if (newValue !== autoSaveEnabled.value) {
          autoSaveEnabled.value = newValue
          
          // 清除现有定时器
          if (autoSaveTimer.value) {
            clearInterval(autoSaveTimer.value)
            autoSaveTimer.value = null
          }
          
          // 如果新设置是开启状态，且没有附件，则启动定时器
          if (newValue && attachments.value.length === 0) {
            startAutoSaveTimer()
            console.log('自动保存草稿功能已启用')
          } else {
            console.log('自动保存草稿功能已禁用')
          }
        }
      }
    }
    
    // 组件挂载时初始化
    onMounted(() => {
      initAutoSave()
      window.addEventListener('storage', handleStorageChange)
      
      // 检查路由中是否有草稿ID
      const draftId = route.query.draftId
      if (draftId) {
        console.log('检测到草稿ID，正在加载草稿...', draftId)
        loadDraft(draftId)
      }
    })
    
    // 组件卸载时清理
    onUnmounted(() => {
      if (autoSaveTimer.value) {
        clearInterval(autoSaveTimer.value)
      }
      window.removeEventListener('storage', handleStorageChange)
    })
    
    return {
      activeSection,
      form,
      attachments,
      user,
      sending,
      saving,
      isUploading,
      fileInput,
      loadingDraft,
      draftLoadError,
      getFileIcon,
      formatFileSize,
      handleAttachmentChange,
      removeAttachment,
      sendMail,
      saveDraft,
      discardDraft,
      handleNavigation,
      handleLogout,
      navigateTo,
      fetchMails
    }
  }
}
</script>

<style scoped>
/* 新增草稿加载样式 */
.loading-draft {
  padding: 10px 15px;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.loading-draft i {
  color: #1890ff;
}

.error-draft {
  padding: 10px 15px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #f5222d;
}

.error-draft i {
  color: #f5222d;
}

/* 原有样式保持不变 */
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

.mail-form {
  background: white;
  border-radius: 8px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
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
  border: 1px solid var(--border);
  border-radius: 5px;
  font-size: 1rem;
}

.form-control:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.15);
}

textarea.form-control {
  min-height: 300px;
  resize: vertical;
}

.attachment-preview {
  margin-top: 10px;
}

.attachment-preview-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
  position: relative;
}

.file-name {
  margin: 0 8px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  color: #666;
  font-size: 0.8em;
}

.remove-attachment {
  margin-left: auto;
  cursor: pointer;
  color: #ff4d4f;
}

.upload-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background-color: #e0e0e0;
  border-radius: 0 0 4px 4px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background-color: #1890ff;
  transition: width 0.3s;
}

.upload-progress span {
  position: absolute;
  right: 5px;
  bottom: -18px;
  font-size: 0.7em;
  color: #666;
}

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

.btn-primary {
  background: var(--primary);
  color: white;
}

.btn-primary:hover {
  background: var(--secondary);
}

.btn-primary:disabled {
  background: #a0a7e6;
  cursor: not-allowed;
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

/* 文件类型图标颜色 */
.fa-file-pdf { color: #f40; }
.fa-file-word { color: #2b579a; }
.fa-file-excel { color: #217346; }
.fa-file-powerpoint { color: #d24726; }
.fa-file-image { color: #87d068; }
.fa-file-archive { color: #fa8c16; }
.fa-file-audio { color: #13c2c2; }
.fa-file-video { color: #722ed1; }
</style>
