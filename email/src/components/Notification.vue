<template>
    <transition name="slide-fade">
      <div 
        v-if="visible"
        class="notification"
        :class="notification.type"
        @click="handleClick"
      >
        <i class="notification-icon" :class="iconClass"></i>
        <div class="notification-content">
          <div class="notification-title">{{ notification.title }}</div>
          <div class="notification-message">{{ notification.message }}</div>
        </div>
        <button class="notification-close" @click="close">&times;</button>
      </div>
    </transition>
  </template>
  
  <script>
  import { ref, computed, onMounted } from 'vue'
  
  export default {
    props: {
      notification: {
        type: Object,
        required: true
      },
      duration: {
        type: Number,
        default: 5000
      }
    },
    setup(props, { emit }) {
      const visible = ref(false)
      let timer = null
  
      const iconClass = computed(() => {
        const icons = {
          'new-mail': 'fas fa-envelope',
          'success': 'fas fa-check-circle',
          'error': 'fas fa-exclamation-circle',
          'warning': 'fas fa-exclamation-triangle'
        }
        return icons[props.notification.type] || 'fas fa-info-circle'
      })
  
      const show = () => {
        visible.value = true
        if (props.duration > 0) {
          timer = setTimeout(() => {
            close()
          }, props.duration)
        }
      }
  
      const close = (event) => {
        event?.stopPropagation()
        clearTimeout(timer)
        visible.value = false
        emit('close')
      }
  
      const handleClick = () => {
        if (props.notification.onClick) {
          props.notification.onClick()
        }
        close()
      }
  
      onMounted(() => {
        show()
      })
  
      return {
        visible,
        iconClass,
        close,
        handleClick
      }
    }
  }
  </script>
  
  <style scoped>
  .notification {
    position: fixed;
    top: 20px;
    right: 20px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    padding: 15px 20px;
    display: flex;
    align-items: center;
    gap: 12px;
    max-width: 400px;
    z-index: 1000;
    transform: translateX(0);
    cursor: pointer;
  }
  
  .notification.new-mail {
    border-left: 4px solid #4cc9f0;
  }
  
  .notification-icon {
    font-size: 1.5rem;
  }
  
  .notification.new-mail .notification-icon {
    color: #4cc9f0;
  }
  
  .notification-content {
    flex: 1;
  }
  
  .notification-title {
    font-weight: 600;
    margin-bottom: 5px;
  }
  
  .notification-message {
    color: #6c757d;
    font-size: 0.9rem;
  }
  
  .notification-close {
    background: none;
    border: none;
    cursor: pointer;
    color: #6c757d;
    font-size: 1.2rem;
    padding: 0 0 0 10px;
  }
  
  .slide-fade-enter-active {
    transition: all 0.3s ease-out;
  }
  
  .slide-fade-leave-active {
    transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
  }
  
  .slide-fade-enter-from,
  .slide-fade-leave-to {
    transform: translateX(100%);
    opacity: 0;
  }
  </style>
  