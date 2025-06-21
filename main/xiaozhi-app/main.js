import App from './App'
import uviewPlus from '@/uni_modules/uview-plus'
import dayjs from 'dayjs'

// 全局过滤器
const filters = {
  // 格式化日期
  formatDate: (value, format = 'YYYY-MM-DD HH:mm:ss') => {
    if (!value) return '';
    return dayjs(value).format(format);
  },
  // 格式化金额
  formatMoney: (value) => {
    if (!value) return '0.00';
    return Number(value).toFixed(2);
  }
};

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
import pinia from './store'
import * as Pinia from 'pinia'

export function createApp() {
  const app = createSSRApp(App);
  
  // 注册pinia
  app.use(pinia);
  
  // 注册uview-plus
  app.use(uviewPlus);
  
  // 注册全局过滤器
  app.config.globalProperties.$filters = filters;
  
  // 全局方法
  app.config.globalProperties.$toast = (title, icon = 'none') => {
    uni.showToast({
      title,
      icon
    });
  };
  
  return {
    app,
    Pinia
  }
}
// #endif