/**
 * 通用工具函数
 */

/**
 * 防抖函数
 * @param {Function} fn 要执行的函数
 * @param {Number} delay 延迟时间，默认500ms
 * @returns {Function} 防抖处理后的函数
 */
export const debounce = (fn, delay = 500) => {
  let timer = null;
  return function(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
};

/**
 * 节流函数
 * @param {Function} fn 要执行的函数
 * @param {Number} delay 延迟时间，默认500ms
 * @returns {Function} 节流处理后的函数
 */
export const throttle = (fn, delay = 500) => {
  let last = 0;
  return function(...args) {
    const now = Date.now();
    if (now - last > delay) {
      last = now;
      fn.apply(this, args);
    }
  };
};

/**
 * 深拷贝
 * @param {Object|Array} obj 要拷贝的对象
 * @returns {Object|Array} 拷贝后的对象
 */
export const deepClone = (obj) => {
  if (obj === null || typeof obj !== 'object') return obj;
  const result = Array.isArray(obj) ? [] : {};
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      result[key] = deepClone(obj[key]);
    }
  }
  return result;
};

/**
 * 格式化日期
 * @param {Date|String|Number} date 日期
 * @param {String} format 格式
 * @returns {String} 格式化后的日期
 */
export const formatDate = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return '';
  const dayjs = require('dayjs');
  return dayjs(date).format(format);
};

/**
 * 格式化金额
 * @param {Number|String} money 金额
 * @param {Number} decimals 小数位数
 * @returns {String} 格式化后的金额
 */
export const formatMoney = (money, decimals = 2) => {
  if (!money) return '0.00';
  return Number(money).toFixed(decimals);
};

/**
 * 获取URL参数
 * @param {String} name 参数名
 * @param {String} url URL
 * @returns {String} 参数值
 */
export const getUrlParam = (name, url) => {
  if (!url) url = window.location.href;
  name = name.replace(/[\[\]]/g, '\\$&');
  const regex = new RegExp(`[?&]${name}(=([^&#]*)|&|#|$)`);
  const results = regex.exec(url);
  if (!results) return null;
  if (!results[2]) return '';
  return decodeURIComponent(results[2].replace(/\+/g, ' '));
};

/**
 * 判断是否为空
 * @param {*} value 要判断的值
 * @returns {Boolean} 是否为空
 */
export const isEmpty = (value) => {
  return (
    value === undefined ||
    value === null ||
    (typeof value === 'string' && value.trim() === '') ||
    (Array.isArray(value) && value.length === 0) ||
    (typeof value === 'object' && Object.keys(value).length === 0)
  );
};

/**
 * 随机字符串
 * @param {Number} length 长度
 * @returns {String} 随机字符串
 */
export const randomString = (length = 16) => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
};

/**
 * 复制文本到剪贴板
 * @param {String} text 要复制的文本
 * @param {Function} callback 回调函数
 */
export const copyToClipboard = (text, callback) => {
  uni.setClipboardData({
    data: text,
    success: () => {
      if (callback) callback();
    }
  });
}; 