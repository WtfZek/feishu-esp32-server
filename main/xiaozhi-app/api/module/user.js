/**
 * 用户API模块
 */
import http from '../http';
import { API } from '../api';

export default {
  /**
   * 登录
   * @param {Object} data 登录数据
   * @returns {Promise} Promise对象
   */
  login(data) {
    return http(API.USER.LOGIN, data, 'POST');
  },
  
  /**
   * 注册
   * @param {Object} data 注册数据
   * @returns {Promise} Promise对象
   */
  register(data) {
    return http(API.USER.REGISTER, data, 'POST');
  },
  
  /**
   * 获取用户信息
   * @returns {Promise} Promise对象
   */
  getUserInfo() {
    return http(API.USER.INFO);
  },
  
  /**
   * 获取验证码
   * @param {String} uuid 唯一标识
   * @returns {Promise} Promise对象
   */
  getCaptcha(uuid) {
    return new Promise((resolve, reject) => {
      // 直接返回图片URL，而不是blob对象
      const captchaUrl = http.baseUrl + API.USER.CAPTCHA + '?uuid=' + uuid + '&t=' + new Date().getTime();
      console.log('验证码URL:', captchaUrl);
      resolve(captchaUrl);
      
      /* 原始方法 - 在某些环境可能不兼容
      uni.request({
        url: http.baseUrl + API.USER.CAPTCHA,
        data: { uuid },
        method: 'GET',
        responseType: 'arraybuffer',
        success: (res) => {
          if (res.statusCode === 200) {
            const blob = new Blob([res.data], { type: 'image/jpeg' });
            resolve(blob);
          } else {
            reject(new Error('获取验证码失败'));
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
      */
    });
  },
  
  /**
   * 获取公共配置
   * @returns {Promise} Promise对象
   */
  getPubConfig() {
    return http(API.USER.PUB_CONFIG);
  },
  
  /**
   * 发送短信验证码
   * @param {Object} data 短信验证码数据
   * @returns {Promise} Promise对象
   */
  sendSmsVerification(data) {
    return http(API.USER.SMS_VERIFICATION, data, 'POST');
  },
  
  /**
   * 修改密码
   * @param {Object} data 密码数据
   * @returns {Promise} Promise对象
   */
  changePassword(data) {
    return http(API.USER.CHANGE_PASSWORD, data, 'PUT');
  },
  
  /**
   * 找回密码
   * @param {Object} data 找回密码数据
   * @returns {Promise} Promise对象
   */
  retrievePassword(data) {
    return http(API.USER.RETRIEVE_PASSWORD, data, 'PUT');
  },
  
  /**
   * 管理员获取用户列表
   * @param {Object} params 查询参数
   * @returns {Promise} Promise对象
   */
  getUserList(params) {
    return http(API.SYSTEM.USERS.PAGE, params);
  },
  
  /**
   * 管理员删除用户
   * @param {Number} id 用户ID
   * @returns {Promise} Promise对象
   */
  deleteUser(id) {
    return http(API.SYSTEM.USERS.DELETE + id, {}, 'DELETE');
  },
  
  /**
   * 管理员重置用户密码
   * @param {Number} id 用户ID
   * @returns {Promise} Promise对象
   */
  resetPassword(id) {
    return http(API.SYSTEM.USERS.RESET_PASSWORD + id, {}, 'PUT');
  },
  
  /**
   * 管理员批量修改用户状态
   * @param {Number} status 状态
   * @param {Array} ids 用户ID数组
   * @returns {Promise} Promise对象
   */
  changeUserStatus(status, ids) {
    return http(API.SYSTEM.USERS.CHANGE_STATUS + status, ids, 'PUT');
  }
}; 