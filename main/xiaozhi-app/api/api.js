/**
 * API接口管理
 */

// 获取服务URL
export function getServiceUrl() {
  return '';
}

// API接口列表
export const API = {
  // 用户相关
  USER: {
    LOGIN: '/user/login/no-captcha',
    REGISTER: '/user/register/no-captcha',
    INFO: '/user/info',
    CAPTCHA: '/user/captcha',
    PUB_CONFIG: '/user/pub-config',
    SMS_VERIFICATION: '/user/smsVerification',
    CHANGE_PASSWORD: '/user/change-password',
    RETRIEVE_PASSWORD: '/user/retrieve-password'
  },
  
  // 智能体相关
  AGENT: {
    LIST: '/agent/list',
    LIST_PUBLISHED: '/agent/list/published',
    CREATE: '/agent',
    DELETE: '/agent/',
    DETAIL: '/agent/',
    UPDATE: '/agent/',
    TEMPLATE: '/agent/template',
    SESSIONS: '/agent/{id}/sessions',
    CHAT_HISTORY: '/agent/{id}/chat-history/',
    AUDIO: '/agent/audio/',
    PLAY: '/agent/play/',
    CLONE: '/agent/clone/',
    MCP_ADDRESS: '/agent/mcp/address/{id}',
    MCP_TOOLS: '/agent/mcp/tools/{id}'
  },
  
  // 设备相关
  DEVICE: {
    BIND: '/device/bind/',
    UNBIND: '/device/unbind',
    REGISTER: '/device/register',
    ENABLE_OTA: '/device/enableOta/',
    UPDATE: '/device/update'
  },
  
  // 模型相关
  MODEL: {
    LIST: '/models/list',
    NAMES: '/models/names',
    DETAIL: '/models/',
    CONFIG: '/models/',
    PROVIDER: '/models/provider',
    VOICES: '/models/{id}/voices',
    CLONE_VOICES: '/models/clone/voices',
    ENABLE: '/models/enable/',
    DEFAULT: '/models/default/',
    VOICE_CLONE: '/models/voice-clone'
  },
  
  // OTA相关
  OTA: {
    LIST: '/otaMag',
    DETAIL: '/otaMag/',
    CREATE: '/otaMag',
    UPDATE: '/otaMag',
    DELETE: '/otaMag',
    UPLOAD: '/otaMag/upload',
    DOWNLOAD_URL: '/otaMag/getDownloadUrl/',
    DOWNLOAD: '/otaMag/download/'
  },

  // 支付相关
  PAY: {
    CREATE_ORDER: '/wechat/pay/create',
    NOTIFY: '/wechat/pay/notify',
    CREATE_CLONE_ORDER: '/wechat/pay/voice-clone/order',
    GET_ORDER_STATUS: '/wechat/pay/voice-clone/order/status/{orderNo}'
  },
  
  // 微信相关
  WECHAT: {
    GET_OPENID: '/wechat/openid'
  },
  
  // 系统相关
  SYSTEM: {
    DICT_TYPE: {
      PAGE: '/admin/dict/type/page',
      DETAIL: '/admin/dict/type/',
      SAVE: '/admin/dict/type/save',
      UPDATE: '/admin/dict/type/update',
      DELETE: '/admin/dict/type/delete'
    },
    DICT_DATA: {
      PAGE: '/admin/dict/data/page',
      DETAIL: '/admin/dict/data/',
      SAVE: '/admin/dict/data/save',
      UPDATE: '/admin/dict/data/update',
      DELETE: '/admin/dict/data/delete',
      TYPE: '/admin/dict/data/type/'
    },
    PARAMS: {
      PAGE: '/admin/params/page',
      DETAIL: '/admin/params/',
      SAVE: '/admin/params',
      UPDATE: '/admin/params',
      DELETE: '/admin/params/delete'
    },
    USERS: {
      PAGE: '/admin/users',
      DELETE: '/admin/users/',
      RESET_PASSWORD: '/admin/users/',
      CHANGE_STATUS: '/admin/users/changeStatus/'
    },
    SERVER: {
      LIST: '/admin/server/server-list',
      EMIT_ACTION: '/admin/server/emit-action'
    }
  }
}; 