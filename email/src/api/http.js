import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 10000
})

// 请求拦截器：简单添加 token（有则加，无则不加）
http.interceptors.request.use(config => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理后端响应格式
http.interceptors.response.use(
  response => {
    // 解构后端统一响应格式
    const { code, message, data } = response.data
    
    // 处理业务逻辑错误（非 200 状态码）
    if (code !== 200) {
      return Promise.reject({
        code,
        message: message || '业务逻辑错误',
        response
      })
    }
    
    // 返回实际数据部分
    return data
  },
  error => {
    // 处理网络错误或服务器错误
    const errorResponse = error.response || {}
    const errorData = errorResponse.data || {}
    
    // 标准化错误对象
    const normalizedError = {
      code: errorData.code || errorResponse.status || 'NETWORK_ERROR',
      message: errorData.message || error.message || '未知错误',
      response: errorResponse
    }
    
    console.error(`API Error [${normalizedError.code}]:`, normalizedError.message)
    return Promise.reject(normalizedError)
  }
)

export default http
