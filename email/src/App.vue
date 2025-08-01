<!-- src/App.vue -->
<template>
  <div id="app" :class="{'dark-mode': darkMode}">
    <!-- 路由视图容器 -->
    <router-view :dark-mode="darkMode" @toggle-dark-mode="toggleDarkMode" />
  </div>
</template>

<script setup>
import { ref, onMounted, provide } from 'vue'
import websocket from '@/utils/websocket'

// 全局深色模式状态
const darkMode = ref(false)

// 从 localStorage 加载深色模式设置
onMounted(() => {
  const savedDarkMode = localStorage.getItem('darkMode')
  if (savedDarkMode !== null) {
    darkMode.value = JSON.parse(savedDarkMode)
    applyDarkMode(darkMode.value)
  }
  
  // 添加全局样式防止选中效果
  addGlobalStyles()
})

// 切换深色模式
const toggleDarkMode = () => {
  darkMode.value = !darkMode.value
  localStorage.setItem('darkMode', JSON.stringify(darkMode.value))
  applyDarkMode(darkMode.value)
}

// 应用深色模式
const applyDarkMode = (isDark) => {
  if (isDark) {
    document.documentElement.classList.add('dark-mode')
  } else {
    document.documentElement.classList.remove('dark-mode')
  }
}

// 添加全局样式防止选中效果
const addGlobalStyles = () => {
  const style = document.createElement('style')
  style.textContent = `
    /* 全局禁用文本选中 */
    body {
      -webkit-user-select: none;
      -moz-user-select: none;
      -ms-user-select: none;
      user-select: none;
    }
    
    /* 允许特定元素内的文本选中 */
    .selectable, .mail-body, .mail-preview, .mail-content, .mail-subject {
      -webkit-user-select: text;
      -moz-user-select: text;
      -ms-user-select: text;
      user-select: text;
    }
    
    /* 禁用触摸高亮 */
    * {
      -webkit-tap-highlight-color: transparent;
    }
    
    /* 禁用邮件项的激活背景色 */
    .mail-item:active {
      background: inherit !important;
    }
    
    /* 禁用按钮的激活状态变化 */
    .btn:active {
      transform: none !important;
      box-shadow: none !important;
    }
    
    /* 禁用输入框的焦点轮廓 */
    input:focus, textarea:focus, button:focus {
      outline: none;
      box-shadow: none;
    }
  `
  document.head.appendChild(style)
}

// 提供深色模式状态给所有子组件
provide('darkMode', darkMode)
provide('toggleDarkMode', toggleDarkMode)

// 其他初始化代码...
</script>

<style>
/* 定义CSS变量 - 浅色模式 */
:root {
  --primary: #4361ee;
  --secondary: #3f37c9;
  --success: #4cc9f0;
  --dark: #1d3557;
  --light: #f8f9fa;
  --gray: #e9ecef;
  --text: #212529;
  --border: #dee2e6;
  --danger: #e63946;
  --warning: #fca311;
  --container-bg: white;
  --content-bg: white;
  --card-bg: white;
  --text-color: #212529;
  --border-color: #dee2e6;
}

/* 深色模式变量 */
.dark-mode {
  --primary: #5a7dff;
  --secondary: #4a5fe0;
  --success: #5ee0ff;
  --dark: #0d1f3d;
  --light: #2d3748;
  --gray: #4a5568;
  --text: #e2e8f0;
  --border: #4a5568;
  --container-bg: #1a202c;
  --content-bg: #2d3748;
  --card-bg: #2d3748;
  --text-color: #e2e8f0;
  --border-color: #4a5568;
}

/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

body {
  background-color: var(--light);
  color: var(--text-color);
  line-height: 1.6;
}

.container {
  display: flex;
  min-height: 100vh;
  max-width: 100%;
  margin: 0 auto;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  background: var(--container-bg);
}

/* 深色模式下的特定样式 */
.dark-mode .mail-list,
.dark-mode .settings-container,
.dark-mode .mail-detail,
.dark-mode .mail-form,
.dark-mode .search-results {
  background-color: var(--card-bg);
  color: var(--text-color);
  border-color: var(--border-color);
}

.dark-mode .mail-item {
  border-bottom-color: var(--border-color);
}

.dark-mode .mail-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
}

.dark-mode .mail-item.unread {
  background-color: rgba(67, 97, 238, 0.1);
}

.dark-mode .section-header {
  border-bottom-color: var(--border-color);
}

.dark-mode .search-bar {
  background-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .search-bar input {
  color: var(--text-color);
}

.dark-mode .btn-outline {
  border-color: var(--primary);
  color: var(--primary);
}

.dark-mode .btn-outline:hover {
  background-color: var(--primary);
  color: white;
}

/* 全局星标样式 - 确保所有页面一致 */
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
  
  /* 允许星标单独交互 */
  pointer-events: auto;
}
</style>
