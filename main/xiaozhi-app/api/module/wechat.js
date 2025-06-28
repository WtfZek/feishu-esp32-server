/**
 * 微信API模块
 */
import http from '../http';
import { API } from '../api';

export default {
  /**
   * 获取微信openid
   * @param {String} code 微信登录code
   * @returns {Promise} Promise对象
   */
  getOpenid(code) {
    return http(API.WECHAT.GET_OPENID, { code });
  }
}; 