/**
 * 表单验证工具函数
 */

/**
 * 验证手机号
 * @param {String} value 手机号
 * @returns {Boolean} 是否通过验证
 */
export const validateMobile = (value) => {
  return /^1[3-9]\d{9}$/.test(value);
};

/**
 * 验证邮箱
 * @param {String} value 邮箱
 * @returns {Boolean} 是否通过验证
 */
export const validateEmail = (value) => {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value);
};

/**
 * 验证URL
 * @param {String} value URL
 * @returns {Boolean} 是否通过验证
 */
export const validateUrl = (value) => {
  return /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w.-]*)*\/?$/.test(value);
};

/**
 * 验证身份证号
 * @param {String} value 身份证号
 * @returns {Boolean} 是否通过验证
 */
export const validateIdCard = (value) => {
  return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(value);
};

/**
 * 验证密码强度
 * @param {String} value 密码
 * @returns {Object} 密码强度信息
 */
export const validatePassword = (value) => {
  // 密码强度：1-弱 2-中 3-强
  let strength = 0;
  
  // 长度大于等于6
  if (value.length >= 6) {
    strength += 1;
  }
  
  // 包含数字和字母
  if (/[0-9]/.test(value) && /[a-zA-Z]/.test(value)) {
    strength += 1;
  }
  
  // 包含特殊字符
  if (/[^0-9a-zA-Z]/.test(value)) {
    strength += 1;
  }
  
  return {
    strength,
    valid: strength >= 2
  };
};

/**
 * 验证是否为空
 * @param {*} value 要验证的值
 * @returns {Boolean} 是否为空
 */
export const validateEmpty = (value) => {
  return (
    value === undefined ||
    value === null ||
    (typeof value === 'string' && value.trim() === '') ||
    (Array.isArray(value) && value.length === 0) ||
    (typeof value === 'object' && Object.keys(value).length === 0)
  );
};

/**
 * 验证是否为数字
 * @param {*} value 要验证的值
 * @returns {Boolean} 是否为数字
 */
export const validateNumber = (value) => {
  return !isNaN(parseFloat(value)) && isFinite(value);
};

/**
 * 验证是否为整数
 * @param {*} value 要验证的值
 * @returns {Boolean} 是否为整数
 */
export const validateInteger = (value) => {
  return Number.isInteger(Number(value));
};

/**
 * 验证是否为正数
 * @param {*} value 要验证的值
 * @returns {Boolean} 是否为正数
 */
export const validatePositive = (value) => {
  return validateNumber(value) && Number(value) > 0;
};

/**
 * 验证是否为MAC地址
 * @param {String} value MAC地址
 * @returns {Boolean} 是否为MAC地址
 */
export const validateMac = (value) => {
  return /^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/.test(value);
}; 