/**
 * OTA API模块
 */
import http from '../http';
import { API } from '../api';

export default {
  /**
   * 获取OTA固件列表
   * @param {Object} params 查询参数
   * @returns {Promise} Promise对象
   */
  getOtaList(params) {
    return http(API.OTA.LIST, params);
  },
  
  /**
   * 获取OTA固件详情
   * @param {String} id OTA固件ID
   * @returns {Promise} Promise对象
   */
  getOtaDetail(id) {
    return http(API.OTA.DETAIL + id);
  },
  
  /**
   * 保存OTA固件信息
   * @param {Object} data OTA固件数据
   * @returns {Promise} Promise对象
   */
  saveOta(data) {
    return http(API.OTA.CREATE, data, 'POST');
  },
  
  /**
   * 更新OTA固件信息
   * @param {String} id OTA固件ID
   * @param {Object} data OTA固件数据
   * @returns {Promise} Promise对象
   */
  updateOta(id, data) {
    return http(API.OTA.UPDATE + id, data, 'PUT');
  },
  
  /**
   * 删除OTA固件
   * @param {String} id OTA固件ID
   * @returns {Promise} Promise对象
   */
  deleteOta(id) {
    return http(API.OTA.DELETE + id, {}, 'DELETE');
  },
  
  /**
   * 上传固件文件
   * @param {Object} file 文件对象
   * @returns {Promise} Promise对象
   */
  uploadFirmware(file) {
    // 使用uni.uploadFile上传文件
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: http.baseUrl + API.OTA.UPLOAD,
        filePath: file.path,
        name: 'file',
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data);
            if (data.code === 0) {
              resolve(data.data);
            } else {
              uni.showToast({
                title: data.msg || '上传失败',
                icon: 'none'
              });
              reject(data.msg);
            }
          } else {
            uni.showToast({
              title: `上传失败：${res.statusCode}`,
              icon: 'none'
            });
            reject(`上传失败：${res.statusCode}`);
          }
        },
        fail: (err) => {
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          });
          reject(err);
        }
      });
    });
  },
  
  /**
   * 获取固件下载链接
   * @param {String} id OTA固件ID
   * @returns {Promise} Promise对象
   */
  getDownloadUrl(id) {
    return http(API.OTA.DOWNLOAD_URL + id);
  },
  
  /**
   * 下载固件文件
   * @param {String} uuid 文件UUID
   * @returns {Promise} Promise对象
   */
  downloadFirmware(uuid) {
    return http(API.OTA.DOWNLOAD + uuid);
  },
  
  /**
   * 启用/关闭OTA自动升级
   * @param {String} id 设备ID
   * @param {Number} status 状态(0关闭/1开启)
   * @returns {Promise} Promise对象
   */
  enableOtaUpgrade(id, status) {
    return http(`/device/enableOta/${id}/${status}`, {}, 'PUT');
  }
}; 