<template>
  <div class="header">
    <div class="search-bar">
      <i class="fas fa-search"></i>
      <input 
        type="text" 
        placeholder="搜索邮件..." 
        v-model="searchQuery"
        @keyup.enter="handleSearch"
      >
      <button 
        class="search-btn"
        @click="handleSearch"
      >
        <i class="fas fa-search"></i>
      </button>
    </div>
    <div class="action-buttons">
      <button class="btn btn-outline" @click="refresh">
        <i class="fas fa-sync-alt"></i> 刷新
      </button>
      <button class="btn btn-primary" @click="compose">
        <i class="fas fa-plus"></i> 写邮件
      </button>
    </div>
  </div>
</template>
<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
export default {
  setup() {
    const router = useRouter()
    const searchQuery = ref('')
    const handleSearch = () => {
      if (searchQuery.value.trim()) {
        router.push({
          name: 'search',
          query: { q: searchQuery.value.trim() }
        })
      }
    }
    return {
      searchQuery,
      handleSearch
    }
  },
  methods: {
    refresh() {
      this.$emit('refresh')
    },
    compose() {
      this.$emit('compose')
    }
  }
}
</script>

<style scoped>

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
.search-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0 10px;
  color: var(--primary);
}
.search-bar {
  position: relative;
}
.search-bar input {
  background: transparent;
  border: none;
  outline: none;
  flex: 1;
  padding: 5px 10px;
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
