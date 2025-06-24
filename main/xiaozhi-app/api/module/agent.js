/**
 * 智能体API模块
 */
import http, { reRequest } from '../http';
import { API } from '../api';

export default {
  /**
   * 获取智能体列表
   * @returns {Promise} Promise对象
   */
  getAgentList() {
    return http(API.AGENT.LIST);
  },

  /**
   * 获取已发布的智能体列表
   * @param {Object} params 分页参数 {page, limit}
   * @param {Object} filter 筛选条件
   * @returns {Promise} Promise对象
   */
  getPublishedAgentList(params = {}, filter = null) {
    return http(API.AGENT.LIST_PUBLISHED, filter, 'POST', { params });
  },
  
  /**
   * 创建智能体
   * @param {Object} data 智能体数据
   * @returns {Promise} Promise对象
   */
  createAgent(data) {
    return http(API.AGENT.CREATE, data, 'POST');
  },
  
  /**
   * 获取智能体详情
   * @param {String} id 智能体ID
   * @returns {Promise} Promise对象
   */
  getAgentDetail(id) {
    return http(API.AGENT.DETAIL + id);
  },
  
  /**
   * 更新智能体
   * @param {String} id 智能体ID
   * @param {Object} data 智能体数据
   * @returns {Promise} Promise对象
   */
  updateAgent(id, data) {
    return http(API.AGENT.UPDATE + id, data, 'PUT');
  },
  
  /**
   * 删除智能体
   * @param {String} id 智能体ID
   * @returns {Promise} Promise对象
   */
  deleteAgent(id) {
    return http(API.AGENT.DELETE + id, {}, 'DELETE');
  },
  
  /**
   * 获取智能体模板列表
   * @returns {Promise} Promise对象
   */
  getAgentTemplates() {
    return http(API.AGENT.TEMPLATE);
  },
  
  /**
   * 获取智能体会话列表
   * @param {String} id 智能体ID
   * @param {Object} params 分页参数
   * @returns {Promise} Promise对象
   */
  getAgentSessions(id, params) {
    return http(API.AGENT.SESSIONS.replace('{id}', id), params);
  },
  
  /**
   * 获取智能体聊天记录
   * @param {String} id 智能体ID
   * @param {String} sessionId 会话ID
   * @returns {Promise} Promise对象
   */
  getAgentChatHistory(id, sessionId) {
    return http(API.AGENT.CHAT_HISTORY.replace('{id}', id) + sessionId);
  },
  
  /**
   * 获取音频下载ID
   * @param {String} audioId 音频ID
   * @returns {Promise} Promise对象
   */
  getAudioId(audioId) {
    return http(API.AGENT.AUDIO + audioId, {}, 'POST');
  },
  
  /**
   * 播放音频
   * @param {String} uuid 音频UUID
   * @returns {Promise} Promise对象
   */
  playAudio(uuid) {
    return http(API.AGENT.PLAY + uuid);
  },
  
  /**
   * 更新智能体记忆
   * @param {String} macAddress 设备MAC地址
   * @param {Object} data 记忆数据
   * @returns {Promise} Promise对象
   */
  updateAgentMemory(macAddress, data) {
    return http(`/agent/saveMemory/${macAddress}`, data, 'PUT');
  },

  /**
   * 复制智能体
   * @param {String} id 智能体ID
   * @returns {Promise} Promise对象
   */
  cloneAgent(id) {
    return http(API.AGENT.CLONE + id, {}, 'POST');
  },
}; 