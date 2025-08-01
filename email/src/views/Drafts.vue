<template>
  <div class="container">
    <AppSidebar 
      :active-section="activeSection" 
      @navigate="handleNavigation"
      @logout="handleLogout"
    />
    
    <div class="main-content">
      <AppHeader @refresh="fetchDrafts" @compose="navigateTo('compose')" />
      
      <div class="content-area">
        <div class="section">
          <div class="section-header">
            <h2 class="section-title">草稿箱</h2>
            <div class="action-buttons">
              <button class="btn btn-outline delete-btn" @click="deleteSelected">
                <i class="fas fa-trash-alt"></i> 删除
              </button>
            </div>
          </div>
          
          <div class="mail-list">
            <div 
              v-for="draft in drafts" 
              :key="draft.id"
              class="mail-item"
              @click="editDraft(draft.id)"
            >
              <div class="mail-checkbox">
                <input type="checkbox" v-model="selectedDrafts" :value="draft.id">
              </div>
              <div class="mail-sender">我</div>
              <div class="mail-content">
                <div class="mail-subject">
                  {{ draft.subject || '(无主题)' }}
                </div>
                <div class="mail-preview">{{ draft.content.substring(0, 50) }}...</div>
              </div>
              <div class="mail-time">{{ formatDate(draft.timestamp) }}</div>
            </div>
            
            <div v-if="loading" class="loading">加载中...</div>
            <div v-if="!loading && drafts.length === 0" class="empty">草稿箱为空</div>
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
import emailApi from '@/api/email.api' // 导入API服务

export default {
  components: {
    AppHeader,
    AppSidebar
  },
  setup() {
    const router = useRouter()
    const drafts = ref([])
    const loading = ref(false)
    const activeSection = ref('drafts')
    const selectedDrafts = ref([])
    
    // 获取草稿数据
    const fetchDrafts = () => {
      loading.value = true
      emailApi.getDrafts()
        .then(data => {
          drafts.value = data
          loading.value = false
        })
        .catch(error => {
          console.error('获取草稿失败:', error)
          loading.value = false
          // 实际项目中可添加错误提示
        })
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleDateString()
    }
    
    // 编辑草稿 - 修改点：只传递草稿ID
    const editDraft = (draftId) => {
      router.push({ 
        name: 'compose',
        query: { draftId }
      })
    }
    
    // 删除选中的草稿
    const deleteSelected = () => {
      if (selectedDrafts.value.length === 0) {
        alert('请选择要删除的草稿')
        return
      }
      
      if (confirm(`确定要删除选中的 ${selectedDrafts.value.length} 份草稿吗？`)) {
        // 创建删除请求数组
        const deletePromises = selectedDrafts.value.map(id => 
          emailApi.deleteMail(id).catch(e => {
            console.error(`删除草稿 ${id} 失败:`, e)
            return false // 标记失败
          })
        )
        
        // 执行批量删除
        Promise.all(deletePromises).then(results => {
          const successCount = results.filter(r => r !== false).length
          if (successCount > 0) {
            // 仅在前端移除成功删除的草稿
            drafts.value = drafts.value.filter(
              draft => !selectedDrafts.value.includes(draft.id) || 
                      results[selectedDrafts.value.indexOf(draft.id)] === false
            )
          }
          selectedDrafts.value = []
          
          if (successCount < selectedDrafts.value.length) {
            alert(`成功删除 ${successCount} 份草稿，部分删除失败`)
          } else {
            alert('草稿删除成功')
          }
        })
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
    
    onMounted(fetchDrafts)
    
    return {
      drafts,
      loading,
      activeSection,
      selectedDrafts,
      fetchDrafts,
      formatDate,
      editDraft,
      deleteSelected,
      handleNavigation,
      handleLogout,
      navigateTo
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
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

.delete-btn {
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
  font-style: italic;
  color: #6c757d;
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
