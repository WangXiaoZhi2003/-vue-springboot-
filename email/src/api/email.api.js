import http from './http'

export default {
  /**
   * 用户注册
   * @param {string} email - 用户邮箱
   * @param {string} password - 密码
   * @returns {Promise<Object>} 包含注册结果的Promise
   */
  register(email, password) {
    return http.post('/api/users/register', { email, password })
  },

  /**
   * 用户登录
   * @param {string} email - 用户邮箱
   * @param {string} password - 密码
   * @returns {Promise<Object>} 包含token的Promise
   */
  login(email, password) {
    return http.post('/api/users/login', { email, password })
  },
  /**
   * 获取草稿邮件
   * @returns {Promise<Array>} 包含草稿邮件列表的Promise
   */
  getDrafts() {
    return http.get('/api/mail/drafts')
  },
  /**
   * 发送邮件
   * @param {string} receiverEmail - 收件人邮箱
   * @param {string} subject - 邮件主题
   * @param {string} content - 邮件内容
   * @returns {Promise<Object>} 包含发送结果的Promise
   */
  sendMail(receiverEmail, subject, content) {
    return http.post('/api/mail/send', {
      receiverEmail,
      subject,
      content
    })
  },

  /**
   * 发送草稿邮件箱
   * @param {string} receiverEmail - 收件人邮箱
   * @param {string} subject - 邮件主题
   * @param {string} content - 邮件内容
   * @returns {Promise<Object>} 包含发送结果的Promise
   */
  senddraftMail(receiverEmail, subject, content) {
    return http.post('/api/mail/senddraft', {
      receiverEmail,
      subject,
      content
    })
  },
  /**
   * 获取收件箱邮件
   * @returns {Promise<Array>} 包含邮件列表的Promise
   */
  getInbox() {

    return http.get('/api/mail/inbox')
  },

  /**
   * 获取已发送邮件
   * @returns {Promise<Array>} 包含已发送邮件列表的Promise
   */
  getSent() {

    return http.get('/api/mail/sent')
  },

  /**
   * 上传附件
   * @param {File|File[]} files - 要上传的文件或文件数组
   * @returns {Promise<Object>} 包含文件路径的Promise
   */
  uploadAttachment(files) {
    const formData = new FormData()

    // 处理单文件或多文件上传
    if (Array.isArray(files)) {
      files.forEach(file => {
        formData.append('file', file)
      })
    } else {
      formData.append('file', files)
    }

    return http.post('/api/attachment/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 修改用户密码
   * @param {string} oldPassword - 原密码
   * @param {string} newPassword - 新密码
   * @returns {Promise<Object>} 包含修改结果的Promise
   */
  repasswd(oldPassword, newPassword) {
    return http.post('/api/users/repasswd', {
      oldPassword,
      newPassword
    })
  },
  /**
     * 删除单个邮件
     * @param {number} mailId - 要删除的邮件ID
     * @returns {Promise<Object>} 包含删除结果的Promise
     */
  deleteMail(mailId) {
    return http.delete(`/api/mail/delete/${mailId}`)
  },

  /**
  * 获取已删除的邮件列表
  * @returns {Promise<Array>} 包含已删除邮件列表的Promise
  */
  getDeleted() {
    return http.get('/api/mail/deleted')
  },
  /**
     * 获取垃圾邮件
     * @returns {Promise<Array>} 包含垃圾邮件列表的Promise
     */
  getSpam() {
    return http.get('/api/mail/spam')
  },
  /**
   * 下载附件
   * @param {string} filename - 要下载的文件名
   * @returns {Promise<Blob>} 包含文件二进制数据的Promise
   */
  downloadAttachment(filename) {
    return http.get(`/api/attachment/download/${filename}`, {
      responseType: 'blob'
    })
  },

  /**
 * 获取邮件详情
 * @param {number} id - 邮件ID
 * @returns {Promise<Object>} 包含邮件详情的Promise
 */
  getMailDetail(id) {
    return http.get(`/api/mail/detail/${id}`)
  },

  /**
    * 搜索邮件（标题/正文）
    * @param {string} keyword - 搜索关键词
    * @returns {Promise<Array>} 包含搜索结果邮件列表的Promise
    */
  searchMail(keyword) {

    return http.get('/api/mail/search', {
      params: { keyword }
    })
  },
  /**
   * 标记邮件为已读
   * @param {string} mailId - 邮件ID
   * @returns {Promise<Object>} 包含操作结果的Promise
   */
  markAsRead(mailId) {
    return http.post(`/api/mail/mark-read/${mailId}`)
  },

  /**
   * 切换邮件星标状态
   * @param {number} mailId - 邮件ID
   * @returns {Promise<Object>} 包含操作结果的Promise
   */
  toggleStar(mailId) {
    return http.post(`/api/mail/toggle-star/${mailId}`)
  },

  /**
     * 获取邮件摘要统计数据
     * @returns {Promise<Object>} 包含统计数据的Promise
     * 返回值示例: { unreadCount: 12, starredCount: 8, sentCount: 24, draftCount: 3 }
     */
  getMailStats() {
    return http.get('/api/mail/stats')
  },
  /**
   * 获取重要邮件列表
   * @returns {Promise<Array>} 包含重要邮件列表的Promise
   * 返回值示例: [
   *   { id: 101, sender: "张经理", subject: "项目审批结果", preview: "...", timestamp: "2025-06-26T09:45:00Z" }
   * ]
   */
  getImportantMails() {
    return http.get('/api/mail/important')
  },
  /**
   * 获取未读邮件摘要
   * @returns {Promise<Array>} 包含未读邮件摘要的Promise
   * 返回值示例: [
   *   { id: 201, sender: "HR部门", subject: "福利计划更新", timestamp: "2025-06-26T10:15:00Z" }
   * ]
   */
  getUnreadMails() {
    return http.get('/api/mail/unread')
  },
  /**
   * 设置邮件过滤规则
   * @param {string} forbiddenKeywords - 禁止关键词（逗号分隔）
   * @returns {Promise<Object>} 包含设置结果的Promise
   */
  setFilterRules(forbiddenKeywords) {
    return http.post('/api/users/rules', {
      forbiddenKeywords
    })
  }
}


