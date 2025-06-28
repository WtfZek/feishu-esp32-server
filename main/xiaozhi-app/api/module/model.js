/**
 * 模型API模块
 */
import http from '../http';
import { API } from '../api';

export default {
  /**
   * 获取模型配置列表
   * @param {Object} params 查询参数
   * @returns {Promise} Promise对象
   */
  getModelConfigList(params) {
    return http(API.MODEL.LIST, params);
  },
  
  /**
   * 获取所有模型名称
   * @param {Object} params 查询参数
   * @returns {Promise} Promise对象
   */
  getModelNames(params) {
    return http(API.MODEL.NAMES, params);
  },
  
  /**
   * 获取模型配置详情
   * @param {String} id 模型ID
   * @returns {Promise} Promise对象
   */
  getModelConfig(id) {
    return http(API.MODEL.DETAIL + id);
  },
  
  /**
   * 添加模型配置
   * @param {String} modelType 模型类型
   * @param {String} provideCode 供应商代码
   * @param {Object} data 模型数据
   * @returns {Promise} Promise对象
   */
  addModelConfig(modelType, provideCode, data) {
    return http(`${API.MODEL.CONFIG}${modelType}/${provideCode}`, data, 'POST');
  },
  
  /**
   * 编辑模型配置
   * @param {String} modelType 模型类型
   * @param {String} provideCode 供应商代码
   * @param {String} id 模型ID
   * @param {Object} data 模型数据
   * @returns {Promise} Promise对象
   */
  editModelConfig(modelType, provideCode, id, data) {
    return http(`${API.MODEL.CONFIG}${modelType}/${provideCode}/${id}`, data, 'PUT');
  },
  
  /**
   * 删除模型配置
   * @param {String} id 模型ID
   * @returns {Promise} Promise对象
   */
  deleteModelConfig(id) {
    return http(API.MODEL.DETAIL + id, {}, 'DELETE');
  },
  
  /**
   * 启用/禁用模型配置
   * @param {String} id 模型ID
   * @param {Number} status 状态(0禁用/1启用)
   * @returns {Promise} Promise对象
   */
  enableModelConfig(id, status) {
    return http(`${API.MODEL.ENABLE}${id}/${status}`, {}, 'PUT');
  },
  
  /**
   * 设置默认模型
   * @param {String} id 模型ID
   * @returns {Promise} Promise对象
   */
  setDefaultModel(id) {
    return http(API.MODEL.DEFAULT + id, {}, 'PUT');
  },
  
  /**
   * 获取模型供应器列表
   * @param {String} modelType 模型类型
   * @returns {Promise} Promise对象
   */
  getModelProviderList(modelType) {
    return http(`${API.MODEL.CONFIG}${modelType}/provideTypes`);
  },
  
  /**
   * 获取模型供应器分页列表
   * @param {Object} params 查询参数
   * @returns {Promise} Promise对象
   */
  getModelProviderPage(params) {
    return http(API.MODEL.PROVIDER, params);
  },
  
  /**
   * 添加模型供应器
   * @param {Object} data 供应器数据
   * @returns {Promise} Promise对象
   */
  addModelProvider(data) {
    return http(API.MODEL.PROVIDER, data, 'POST');
  },
  
  /**
   * 修改模型供应器
   * @param {Object} data 供应器数据
   * @returns {Promise} Promise对象
   */
  editModelProvider(data) {
    return http(API.MODEL.PROVIDER, data, 'PUT');
  },
  
  /**
   * 删除模型供应器
   * @param {Array} ids ID数组
   * @returns {Promise} Promise对象
   */
  deleteModelProvider(ids) {
    return http(API.MODEL.PROVIDER + '/delete', ids, 'POST');
  },
  
  /**
   * 获取模型音色列表
   * @param {String} modelId 模型ID
   * @param {String} voiceName 音色名称(可选)
   * @returns {Promise} Promise对象
   */
  getVoiceList(modelId, voiceName) {
    const params = voiceName ? { voiceName } : {};
    return http(API.MODEL.VOICES.replace('{id}', modelId), params);
  },
  
  /**
   * 获取克隆音色列表
   * @returns {Promise} Promise对象
   */
  getCloneVoiceList() {
    return http(API.MODEL.CLONE_VOICES);
  },
  
  /**
   * 克隆语音
   * @param {Object} formData 表单数据
   * @param {Function} onProgress 上传进度回调
   * @returns {Promise} Promise对象
   */
  cloneVoice(formData, onProgress) {
    // 获取baseUrl
    const baseUrl = http.baseUrl || 'http://192.168.3.188:8002/xiaozhi';
    const apiUrl = baseUrl + API.MODEL.VOICE_CLONE;
    
    // #ifdef MP-WEIXIN
    if (!formData.voiceFile || !formData.voiceFile.path) {
      return Promise.reject(new Error('文件路径不存在'));
    }
    
    return new Promise((resolve, reject) => {
      const requestData = {
        name: formData.name,
        languages: formData.languages,
        remark: formData.remark || ''
      };
      
      const token = uni.getStorageSync('token');
      
      // 使用微信原生上传API
      wx.uploadFile({
        url: apiUrl,
        filePath: formData.voiceFile.path,
        name: 'voiceFile',
        formData: requestData,
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            resolve(data);
          } catch (e) {
            reject(new Error('解析响应数据失败'));
          }
        },
        fail: (err) => {
          // 如果wx.uploadFile失败，尝试使用uni.uploadFile
          uni.uploadFile({
            url: apiUrl,
            filePath: formData.voiceFile.path,
            name: 'voiceFile',
            formData: requestData,
            header: {
              'Authorization': 'Bearer ' + token
            },
            success: (res) => {
              try {
                const data = JSON.parse(res.data);
                resolve(data);
              } catch (e) {
                reject(new Error('解析响应数据失败'));
              }
            },
            fail: (err) => {
              reject(err);
            }
          });
        }
      });
    });
    // #endif
    
    // #ifdef H5
    if (!formData.voiceFile || !formData.voiceFile.file) {
      return Promise.reject(new Error('文件对象不存在'));
    }
    
    const formDataH5 = new FormData();
    formDataH5.append('name', formData.name);
    formDataH5.append('languages', formData.languages);
    formDataH5.append('remark', formData.remark || '');
    formDataH5.append('voiceFile', formData.voiceFile.file);
    
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token');
      
      const xhr = new XMLHttpRequest();
      xhr.open('POST', apiUrl, true);
      xhr.setRequestHeader('Authorization', 'Bearer ' + token);
      
      // 进度回调
      if (typeof onProgress === 'function') {
        xhr.upload.onprogress = (event) => {
          if (event.lengthComputable) {
            const percent = Math.floor(event.loaded * 100 / event.total);
            onProgress(percent);
          }
        };
      }
      
      xhr.onload = function() {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const response = JSON.parse(xhr.responseText);
            resolve(response);
          } catch (e) {
            reject(new Error('解析响应数据失败'));
          }
        } else {
          reject(new Error('请求失败，状态码:' + xhr.status));
        }
      };
      
      xhr.onerror = function() {
        reject(new Error('网络错误'));
      };
      
      xhr.send(formDataH5);
    });
    // #endif
    
    // #ifdef APP-PLUS
    if (!formData.voiceFile || !formData.voiceFile.path) {
      return Promise.reject(new Error('文件路径不存在'));
    }
    
    return new Promise((resolve, reject) => {
      const requestData = {
        name: formData.name,
        languages: formData.languages,
        remark: formData.remark || ''
      };
      
      const token = uni.getStorageSync('token');
      
      uni.uploadFile({
        url: apiUrl,
        filePath: formData.voiceFile.path,
        name: 'voiceFile',
        formData: requestData,
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            resolve(data);
          } catch (e) {
            reject(new Error('解析响应数据失败'));
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
    // #endif
  }
}; 