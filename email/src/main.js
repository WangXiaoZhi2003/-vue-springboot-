import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import websocket from '@/utils/websocket'
// 引入 Font Awesome CSS
import '@fortawesome/fontawesome-free/css/all.min.css'

const app = createApp(App)

app.use(router)
app.mount('#app')

const userData = localStorage.getItem('user_info')
if (userData) {
  const user = JSON.parse(userData)
  websocket.connect(user.email)
}
router.beforeEach((to, from, next) => {
    websocket.ensureNotificationContainer();
    next();
  });