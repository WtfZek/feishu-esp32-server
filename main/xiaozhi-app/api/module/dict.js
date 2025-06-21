/**
 * 字典API模块
 */
import http from '../http';
import { API } from '../api';

export default {
  /**
   * 获取字典类型分页列表
   * @param {Object} params 查询参数
   * @returns {Promise} Promise对象
   */
  getDictTypePage(params) {
    return http(API.SYSTEM.DICT_TYPE.PAGE, params);
  },
  
  /**
   * 获取字典类型详情
   * @param {Number} id 字典类型ID
   * @returns {Promise} Promise对象
   */
  getDictTypeDetail(id) {
    return http(API.SYSTEM.DICT_TYPE.DETAIL + id);
  },
  
  /**
   * 保存字典类型
   * @param {Object} data 字典类型数据
   * @returns {Promise} Promise对象
   */
  saveDictType(data) {
    return http(API.SYSTEM.DICT_TYPE.SAVE, data, 'POST');
  },
  
  /**
   * 更新字典类型
   * @param {Object} data 字典类型数据
   * @returns {Promise} Promise对象
   */
  updateDictType(data) {
    return http(API.SYSTEM.DICT_TYPE.UPDATE, data, 'PUT');
  },
  
  /**
   * 删除字典类型
   * @param {Array} ids ID数组
   * @returns {Promise} Promise对象
   */
  deleteDictType(ids) {
    return http(API.SYSTEM.DICT_TYPE.DELETE, ids, 'POST');
  },
  
  /**
   * 获取字典数据分页列表
   * @param {Object} params 查询参数
   * @returns {Promise} Promise对象
   */
  getDictDataPage(params) {
    return http(API.SYSTEM.DICT_DATA.PAGE, params);
  },
  
  /**
   * 获取字典数据详情
   * @param {Number} id 字典数据ID
   * @returns {Promise} Promise对象
   */
  getDictDataDetail(id) {
    return http(API.SYSTEM.DICT_DATA.DETAIL + id);
  },
  
  /**
   * 根据字典类型获取字典数据列表
   * @param {String} dictType 字典类型
   * @returns {Promise} Promise对象
   */
  getDictDataByType(dictType) {
    return http(API.SYSTEM.DICT_DATA.TYPE + dictType);
  },
  
  /**
   * 保存字典数据
   * @param {Object} data 字典数据
   * @returns {Promise} Promise对象
   */
  saveDictData(data) {
    return http(API.SYSTEM.DICT_DATA.SAVE, data, 'POST');
  },
  
  /**
   * 更新字典数据
   * @param {Object} data 字典数据
   * @returns {Promise} Promise对象
   */
  updateDictData(data) {
    return http(API.SYSTEM.DICT_DATA.UPDATE, data, 'PUT');
  },
  
  /**
   * 删除字典数据
   * @param {Array} ids ID数组
   * @returns {Promise} Promise对象
   */
  deleteDictData(ids) {
    return http(API.SYSTEM.DICT_DATA.DELETE, ids, 'POST');
  },
  
  /**
   * 获取参数分页列表
   * @param {Object} params 查询参数
   * @returns {Promise} Promise对象
   */
  getParamsPage(params) {
    return http(API.SYSTEM.PARAMS.PAGE, params);
  },
  
  /**
   * 获取参数详情
   * @param {Number} id 参数ID
   * @returns {Promise} Promise对象
   */
  getParamsDetail(id) {
    return http(API.SYSTEM.PARAMS.DETAIL + id);
  },
  
  /**
   * 保存参数
   * @param {Object} data 参数数据
   * @returns {Promise} Promise对象
   */
  saveParams(data) {
    return http(API.SYSTEM.PARAMS.SAVE, data, 'POST');
  },
  
  /**
   * 更新参数
   * @param {Object} data 参数数据
   * @returns {Promise} Promise对象
   */
  updateParams(data) {
    return http(API.SYSTEM.PARAMS.UPDATE, data, 'PUT');
  },
  
  /**
   * 删除参数
   * @param {Array} ids ID数组
   * @returns {Promise} Promise对象
   */
  deleteParams(ids) {
    return http(API.SYSTEM.PARAMS.DELETE, ids, 'POST');
  }
}; 