/**
 * 设备API模块
 */
import http from '../http';
import { API } from '../api';

export default {
  /**
   * 获取已绑定设备
   * @param {String} agentId 智能体ID
   * @returns {Promise} Promise对象
   */
  getUserDevices(agentId) {
    return http(API.DEVICE.BIND + agentId);
  },
  
  /**
   * 绑定设备
   * @param {String} agentId 智能体ID
   * @param {String} deviceCode 设备编码
   * @returns {Promise} Promise对象
   */
  bindDevice(agentId, deviceCode) {
    return http(API.DEVICE.BIND + agentId + '/' + deviceCode, {}, 'POST');
  },
  
  /**
   * 解绑设备
   * @param {Object} data 解绑数据
   * @returns {Promise} Promise对象
   */
  unbindDevice(data) {
    return http(API.DEVICE.UNBIND, data, 'POST');
  },
  
  /**
   * 注册设备
   * @param {Object} data 设备数据
   * @returns {Promise} Promise对象
   */
  registerDevice(data) {
    return http(API.DEVICE.REGISTER, data, 'POST');
  },
  
  /**
   * 启用/关闭OTA自动升级
   * @param {String} id 设备ID
   * @param {Number} status 状态(0关闭/1开启)
   * @returns {Promise} Promise对象
   */
  enableOtaUpgrade(id, status) {
    return http(API.DEVICE.ENABLE_OTA + id + '/' + status, {}, 'PUT');
  },
  
  /**
   * 管理员获取所有设备
   * @param {Object} params 查询参数
   * @returns {Promise} Promise对象
   */
  getAllDevices(params) {
    return http('/admin/device/all', params);
  },
  
  /**
   * 更新设备信息
   * @param {String} id 设备ID
   * @param {Object} data 更新数据
   * @returns {Promise} Promise对象
   */
  updateDeviceInfo(id, data) {
    return http(API.DEVICE.UPDATE + '/' + id, data, 'PUT');
  }
}; 