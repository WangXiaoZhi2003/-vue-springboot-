// websocket.js
import { ref } from 'vue'

class WebSocketService {
  constructor() {
    this.socket = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 5000
    this.isConnected = ref(false)
    this.notificationContainer = null
    this.currentEmail = null // 存储当前连接的邮箱
    this.settingChangeListenerAdded = false // 标记是否已添加设置监听器
  }

  connect(email) {
    if (this.socket) {
      this.disconnect()
    }
    
    this.currentEmail = email // 保存当前邮箱
    this.initSettingListener() // 初始化设置监听
    
    const token = localStorage.getItem('auth_token');
    const wsUrl = `ws://localhost:8081/ws/mail/${email}?token=${token}`
    this.socket = new WebSocket(wsUrl)

    this.socket.onopen = () => {
      console.log('WebSocket连接成功')
      this.isConnected.value = true
      this.reconnectAttempts = 0
    }

    this.socket.onmessage = (event) => {
      this.handleMessage(event.data)
    }

    this.socket.onclose = (event) => {
      console.log('WebSocket连接关闭', event)
      this.isConnected.value = false
      this.reconnect(email)
    }

    this.socket.onerror = (error) => {
      console.error('WebSocket错误:', error)
      this.isConnected.value = false
    }
    
    // 确保通知容器存在
    this.ensureNotificationContainer()
  }

  // 初始化设置监听
  initSettingListener() {
    if (this.settingChangeListenerAdded) return;
    
    // 监听 storage 事件，跨标签页同步设置
    window.addEventListener('storage', (event) => {
      if (event.key === 'mailSettings') {
        console.log('检测到设置变化，重新连接 WebSocket');
        if (this.currentEmail) {
          this.reconnect(this.currentEmail);
        }
      }
    });
    
    this.settingChangeListenerAdded = true;
  }

  handleMessage(data) {
    try {
      const message = JSON.parse(data)
      switch (message.type) {
        case 'NEW_MAIL':
          this.showNewMailNotification(message)
          break
        default:
          // 无论收到什么类型的消息，都显示为新邮件通知
          this.showSimpleNotification('您收到一封新邮件', data)
      }
    } catch (e) {
      // 如果解析失败，直接显示原始数据
      this.showSimpleNotification('新邮件通知', data)
    }
  }
  
  // 创建通知容器（改进版）
  ensureNotificationContainer() {
    // 先检查容器是否已存在
    let container = document.getElementById('ws-notifications');
    
    if (container) {
      this.notificationContainer = container;
      return;
    }
    
    // 如果不存在则创建新容器
    this.notificationContainer = document.createElement('div');
    this.notificationContainer.id = 'ws-notifications';
    this.notificationContainer.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
    `;
    
    document.body.appendChild(this.notificationContainer);
    console.log('已创建通知容器');
  }

  // 检查是否启用通知
  isNotificationEnabled() {
    try {
      const settings = localStorage.getItem('mailSettings')
      if (!settings) return true // 默认开启通知
      
      const parsedSettings = JSON.parse(settings)
      return parsedSettings.emailNotification !== false
    } catch (e) {
      console.error('读取设置失败:', e)
      return true // 出错时默认开启
    }
  }

  // 显示新邮件通知
  showNewMailNotification(message) {
    console.log('收到新邮件通知，检查通知设置...');
    
    if (!this.isNotificationEnabled()) {
      console.log('通知功能已禁用，跳过显示')
      return
    }
    
    console.log('通知功能已启用，显示通知')
    this.showSimpleNotification(
      '新邮件通知',
      `来自: ${message.from || '未知发件人'}\n主题: ${message.subject || '无主题'}`
    )
  }
  
  // 显示简单通知
  showSimpleNotification(title, content) {
    // 确保通知容器存在
    this.ensureNotificationContainer()
    
    if (!this.isNotificationEnabled()) {
      console.log('通知功能已禁用，跳过显示')
      return
    }
    
    const notification = document.createElement('div')
    notification.className = 'ws-notification'
    notification.style.cssText = `
      background: white;
      border-left: 4px solid #4361ee;
      padding: 15px;
      border-radius: 4px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      min-width: 300px;
      cursor: pointer;
      transition: all 0.3s ease;
    `
    
    notification.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <h3 style="margin: 0; font-size: 16px; color: #4361ee;">${title}</h3>
        <button style="background: none; border: none; font-size: 18px; cursor: pointer;">×</button>
      </div>
      <div style="margin-top: 10px;">
        <pre style="margin: 0; white-space: pre-wrap; font-family: inherit;">${content}</pre>
      </div>
    `
    
    // 添加点击事件
    notification.querySelector('button').addEventListener('click', (e) => {
      e.stopPropagation()
      notification.remove()
    })
    
    notification.addEventListener('click', (e) => {
      if (e.target.tagName !== 'BUTTON') {
        window.location.href = '/inbox'
      }
    })
    
    // 添加到容器
    this.notificationContainer.appendChild(notification)
    
    // 5秒后自动关闭
    setTimeout(() => {
      if (notification.parentNode) {
        notification.remove()
      }
    }, 5000)
  }

  reconnect(email) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重新连接 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      setTimeout(() => this.connect(email), this.reconnectInterval)
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.close()
      this.socket = null
      this.isConnected.value = false
    }
  }
}

export default new WebSocketService()
