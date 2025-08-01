<template>
  <div class="container">
    <!-- 侧边栏导航 -->
    <AppSidebar :active-section="activeSection" @navigate="handleNavigation" @logout="handleLogout" />

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 头部 -->
      <div class="header">
        <div class="search-bar">
          <i class="fas fa-search"></i>
          <input type="text" placeholder="搜索邮件..." v-model="searchQuery">
          <button @click="performSearch"><i class="fas fa-search"></i></button>
        </div>
        <div class="action-buttons">
          <button class="btn btn-outline" @click="refreshSettings">
            <i class="fas fa-sync-alt"></i> 刷新
          </button>
          <button class="btn btn-primary" @click="navigateTo('compose')">
            <i class="fas fa-plus"></i> 写邮件
          </button>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-area">
        <div class="section-header">
          <h2 class="section-title">系统设置</h2>
        </div>

        <div class="settings-container">
          <div class="setting-item">
            <div class="setting-info">
              <h3>邮件通知</h3>
              <p>接收新邮件时显示通知</p>
            </div>
            <label class="toggle-switch">
              <input type="checkbox" v-model="settings.emailNotification" @change="saveSettings">
              <span class="slider"></span>
            </label>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h3>自动保存草稿</h3>
              <p>每5分钟自动保存草稿邮件</p>
            </div>
            <label class="toggle-switch">
              <input type="checkbox" v-model="settings.autoSaveDrafts" @change="saveSettings">
              <span class="slider"></span>
            </label>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h3>深色模式</h3>
              <p>切换到深色主题</p>
            </div>
            <label class="toggle-switch">
              <input type="checkbox" v-model="settings.darkMode" @change="toggleDarkMode">
              <span class="slider"></span>
            </label>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h3>自动回复</h3>
              <p>设置不在办公室时的自动回复</p>
            </div>
            <label class="toggle-switch">
              <input type="checkbox" v-model="settings.autoReply" @change="saveSettings">
              <span class="slider"></span>
            </label>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h3>邮件签名</h3>
              <p>设置默认邮件签名</p>
            </div>
            <button class="btn btn-outline" @click="editSignature">
              <i class="fas fa-edit"></i> 编辑签名
            </button>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h3>邮件过滤规则</h3>
              <p>管理垃圾邮件过滤规则</p>
            </div>
            <button class="btn btn-outline" @click="manageFilters">
              <i class="fas fa-filter"></i> 管理规则
            </button>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <h3>账户安全</h3>
              <p>更改密码和安全设置</p>
            </div>
            <button class="btn btn-outline" @click="manageSecurity">
              <i class="fas fa-lock"></i> 安全设置
            </button>
          </div>
        </div>

        <!-- 签名编辑模态框 -->
        <div v-if="showSignatureModal" class="modal">
          <div class="modal-content">
            <div class="modal-header">
              <h3>编辑邮件签名</h3>
              <button class="modal-close" @click="showSignatureModal = false">&times;</button>
            </div>
            <div class="form-group">
              <textarea v-model="signatureContent" class="form-control" rows="6" placeholder="输入您的邮件签名..."></textarea>
            </div>
            <div class="action-buttons">
              <button class="btn btn-primary" @click="saveSignature">保存</button>
              <button class="btn btn-outline" @click="showSignatureModal = false">取消</button>
            </div>
          </div>
        </div>

        <!-- 账户安全模态框 -->
        <div v-if="showSecurityModal" class="modal">
          <div class="modal-content">
            <div class="modal-header">
              <h3>修改密码</h3>
              <button class="modal-close" @click="showSecurityModal = false">&times;</button>
            </div>
            <div class="form-group">
              <label>账号</label>
              <input type="text" :value="account" class="form-control" readonly>
            </div>
            <div class="form-group">
              <label>原密码</label>
              <input type="password" v-model="currentPassword" class="form-control" placeholder="请输入原密码">
            </div>
            <div class="form-group">
              <label>新密码</label>
              <input type="password" v-model="newPassword" class="form-control" placeholder="请输入新密码">
            </div>
            <div class="action-buttons">
              <button class="btn btn-primary" @click="changePassword">确定</button>
              <button class="btn btn-outline" @click="showSecurityModal = false">取消</button>
            </div>
          </div>
        </div>

        <!-- 新增：过滤规则模态框 -->
        <div v-if="showFilterModal" class="modal">
          <div class="modal-content">
            <div class="modal-header">
              <h3>设置邮件过滤规则</h3>
              <button class="modal-close" @click="showFilterModal = false">&times;</button>
            </div>
            <div class="form-group">
              <label>禁止关键词（用逗号分隔）</label>
              <textarea v-model="filterKeywords" class="form-control" rows="4"
                placeholder="例如: 促销,免费,特价,优惠券"></textarea>
              <p class="hint">输入的关键词将同时过滤邮件主题和内容</p>
            </div>
            <div class="action-buttons">
              <button class="btn btn-primary" @click="saveFilterRules">保存规则</button>
              <button class="btn btn-outline" @click="showFilterModal = false">取消</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AppSidebar from '@/components/layout/Sidebar.vue';
import api from '@/api/email.api.js';

export default {
  components: {
    AppSidebar
  },
  setup() {
    const router = useRouter();

    // 页面状态
    const activeSection = ref('settings');
    const searchQuery = ref('');
    const showSignatureModal = ref(false);
    const signatureContent = ref('');

    // 过滤规则相关状态
    const showFilterModal = ref(false);
    const filterKeywords = ref('');

    // 账户安全相关状态
    const showSecurityModal = ref(false);
    const account = ref('');
    const currentPassword = ref('');
    const newPassword = ref('');

    // 用户信息
    const user = ref({
      name: '1测试用户',
      email: 'test@example.com'
    });

    // 设置数据
    const settings = ref({
      emailNotification: true,
      autoSaveDrafts: true,
      darkMode: false,
      autoReply: false,
      signature: '此致，\n[您的姓名]'
    });

    const loadSettings = () => {
      // 尝试加载完整设置
      const savedSettings = localStorage.getItem('mailSettings');

      if (savedSettings) {
        try {
          settings.value = JSON.parse(savedSettings);
        } catch (e) {
          console.error('解析设置失败:', e);
          resetToDefaultSettings();
        }
      } else {
        resetToDefaultSettings();
      }

      // 兼容旧版自动保存草稿设置
      const autoSaveEnabled = localStorage.getItem('autoSaveDraftEnabled');
      if (autoSaveEnabled !== null) {
        settings.value.autoSaveDrafts = autoSaveEnabled === 'true';
      }
    };

    const saveSettings = () => {
      // 保存设置
      localStorage.setItem('mailSettings', JSON.stringify(settings.value));
      localStorage.setItem('autoSaveDraftEnabled', settings.value.autoSaveDrafts);

      // 添加事件触发设置变化
      const event = new Event('storage');
      event.key = 'mailSettings';
      window.dispatchEvent(event);

      console.log('设置已保存并广播变更');
      // 如果关闭通知，清除所有现有通知
      if (!settings.value.emailNotification) {
        const container = document.getElementById('ws-notifications');
        if (container) {
          container.innerHTML = '';
          console.log('已清除所有通知');
        }
      } else {
        console.log('通知功能已启用');
      }
    };

    // 切换深色模式
    const toggleDarkMode = () => {
      saveSettings();
      if (settings.value.darkMode) {
        document.documentElement.classList.add('dark-mode');
      } else {
        document.documentElement.classList.remove('dark-mode');
      }
    };

    // 编辑签名
    const editSignature = () => {
      signatureContent.value = settings.value.signature;
      showSignatureModal.value = true;
    };

    // 保存签名
    const saveSignature = () => {
      settings.value.signature = signatureContent.value;
      saveSettings();
      showSignatureModal.value = false;
    };

    // 管理过滤规则
    const manageFilters = () => {
      showFilterModal.value = true;
    };

    // 保存过滤规则
    const saveFilterRules = async () => {
      if (!filterKeywords.value.trim()) {
        alert('请输入至少一个过滤关键词');
        return;
      }

      try {
        // 调用API接口
        await api.setFilterRules(filterKeywords.value);

        alert('过滤规则设置成功！');
        showFilterModal.value = false;
      } catch (error) {
        console.error('保存过滤规则失败:', error);
        alert(`保存失败: ${error.message || '未知错误'}`);
      }
    };

    // 管理安全设置
    const manageSecurity = () => {
      // 从localStorage获取用户名
      const userInfo = JSON.parse(localStorage.getItem('user_info'));
      if (userInfo && userInfo.username) {
        account.value = userInfo.username;
      } else {
        account.value = '未知用户';
      }
      // 重置表单
      currentPassword.value = '';
      newPassword.value = '';
      showSecurityModal.value = true;
    };

    const changePassword = async () => {
      if (!currentPassword.value || !newPassword.value) {
        alert('请填写原密码和新密码');
        return;
      }

      try {
        // 调用 API
        const result = await api.repasswd(currentPassword.value, newPassword.value);

        // 处理成功响应
        alert('密码修改成功！');
        showSecurityModal.value = false;
      } catch (error) {
        // 处理错误响应
        if (error.code === 200) {
          // 特殊处理：拦截器将200状态视为错误的情况
          alert('密码修改成功！');
          showSecurityModal.value = false;
        } else {
          // 显示实际错误消息
          const errorMsg = error.message || '密码修改失败';
          alert(`请求错误: ${errorMsg}`);
        }
      }
    };

    // 导航处理
    const handleNavigation = (section) => {
      activeSection.value = section;
      router.push({ name: section });
    };

    // 导航到指定页面
    const navigateTo = (section) => {
      activeSection.value = section;
      router.push({ name: section });
    };

    // 退出登录
    const handleLogout = () => {
      router.push({ name: 'auth' });
    };

    // 刷新设置
    const refreshSettings = () => {
      loadSettings();
    };

    // 搜索功能
    const performSearch = () => {
      if (searchQuery.value.trim()) {
        router.push({
          name: 'search',
          query: { q: searchQuery.value }
        });
      }
    };

    // 初始化
    onMounted(() => {
      loadSettings();
      localStorage.setItem('autoSaveDraftEnabled', settings.value.autoSaveDrafts);
      if (settings.value.darkMode) {
        document.documentElement.classList.add('dark-mode');
      }
    });

    return {
      activeSection,
      searchQuery,
      user,
      settings,
      showSignatureModal,
      signatureContent,
      showSecurityModal,
      account,
      currentPassword,
      newPassword,
      showFilterModal,
      filterKeywords,
      loadSettings,
      saveSettings,
      toggleDarkMode,
      editSignature,
      saveSignature,
      manageFilters,
      saveFilterRules,
      manageSecurity,
      handleNavigation,
      navigateTo,
      handleLogout,
      refreshSettings,
      performSearch,
      changePassword
    };
  }
};
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

.header {
  padding: 15px 30px;
  background: white;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-bar {
  display: flex;
  align-items: center;
  background: var(--gray);
  border-radius: 30px;
  padding: 8px 15px;
  width: 400px;
}

.search-bar input {
  background: transparent;
  border: none;
  outline: none;
  flex: 1;
  padding: 5px 10px;
}

.search-bar button {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--primary);
  font-size: 1rem;
}

.action-buttons {
  display: flex;
  gap: 15px;
  align-items: center;
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

.btn-outline {
  background: transparent;
  border: 1px solid var(--primary);
  color: var(--primary);
}

.btn-outline:hover {
  background: var(--primary);
  color: white;
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
}

.settings-container {
  background: white;
  border-radius: 8px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.setting-item {
  padding: 15px 0;
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info h3 {
  margin-bottom: 5px;
}

.setting-info p {
  color: #6c757d;
  font-size: 0.9rem;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked+.slider {
  background-color: var(--primary);
}

input:checked+.slider:before {
  transform: translateX(26px);
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: white;
  border-radius: 10px;
  padding: 25px;
  width: 500px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border);
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
}

.form-group {
  margin-bottom: 20px;
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

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 新增提示文本样式 */
.hint {
  font-size: 0.85rem;
  color: #6c757d;
  margin-top: 5px;
}

/* 深色模式样式 */
.dark-mode .settings-container,
.dark-mode .modal-content {
  background: #2d3748;
  color: #e2e8f0;
}

.dark-mode .setting-info p {
  color: #a0aec0;
}

.dark-mode .section-header {
  border-bottom: 1px solid #4a5568;
}

.dark-mode .setting-item {
  border-bottom: 1px solid #4a5568;
}

.dark-mode .form-control {
  background: #4a5568;
  border-color: #4a5568;
  color: #e2e8f0;
}

.dark-mode .form-control:focus {
  border-color: var(--primary);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .search-bar {
    width: 300px;
  }
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .search-bar {
    width: 100%;
  }

  .action-buttons {
    align-self: flex-end;
  }
}
</style>
