/**
 * 用户状态管理
 */
import { defineStore } from 'pinia';
import userApi from '../api/module/user';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: null,
    userInfo: null,
    isAdmin: false
  }),
  
  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,
    // 获取用户信息
    getUserInfo: (state) => state.userInfo
  },
  
  actions: {
    // 设置Token
    setToken(token) {
      this.token = token;
      uni.setStorageSync('token', token);
    },
    
    // 清除Token
    clearToken() {
      this.token = null;
      this.userInfo = null;
      this.isAdmin = false;
      uni.removeStorageSync('token');
    },
    
    // 登录
    async login(data) {
      try {
        const res = await userApi.login(data);
        this.setToken(res.token);
        await this.getUserInfoAction();
        return res;
      } catch (error) {
        throw error;
      }
    },
    
    // 注册
    async register(data) {
      try {
        return await userApi.register(data);
      } catch (error) {
        throw error;
      }
    },
    
    // 获取用户信息
    async getUserInfoAction() {
      try {
        const res = await userApi.getUserInfo();
        this.userInfo = res;
        this.isAdmin = res.superAdmin === 1;
        return res;
      } catch (error) {
        throw error;
      }
    },
    
    // 退出登录
    logout() {
      this.clearToken();
      // 跳转到登录页
      uni.reLaunch({
        url: '/pages/login/index'
      });
    }
  },
  
  // 数据持久化
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'user-store',
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
      }
    ]
  }
}); 