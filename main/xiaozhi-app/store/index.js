/**
 * Pinia状态管理
 */
import { createPinia } from 'pinia';
import { createPersistedState } from 'pinia-plugin-persistedstate';

// 创建pinia实例
const pinia = createPinia();

// 数据持久化插件
pinia.use(createPersistedState({
  storage: {
    getItem: (key) => {
      return uni.getStorageSync(key);
    },
    setItem: (key, value) => {
      uni.setStorageSync(key, value);
    },
    removeItem: (key) => {
      uni.removeStorageSync(key);
    }
  }
}));

export default pinia; 