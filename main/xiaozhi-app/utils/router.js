// 页面路由定义
export const ROUTES = {
  // 首页
  HOME: {
    path: '/pages/agent/index',
    name: '智能体管理'
  },
  
  // 登录页
  LOGIN: {
    path: '/pages/login/index',
    name: '登录'
  },
  
  // 用户中心
  USER: {
    path: '/pages/my/index',
    name: '我的'
  },
  
  // 语音管理
  VOICE: {
    INDEX: {
      path: '/pages/manager/voice/index',
      name: '音色库'
    },
    CLONE: {
      path: '/pages/manager/voice/clone',
      name: '语音克隆'
    },
    DETAIL: {
      path: '/pages/manager/voice/detail',
      name: '音色详情'
    }
  },
  
  // 智能体管理
  AGENT: {
    INDEX: {
      path: '/pages/agent/index',
      name: '智能体管理'
    },
    CREATE: {
      path: '/pages/agent/create',
      name: '角色配置'
    },
    EDIT: {
      path: '/pages/agent/edit',
      name: '角色配置'
    },
    DETAIL: {
      path: '/pages/agent/detail',
      name: '角色配置'
    }
  },
  
  // 设备管理
  DEVICE: {
    INDEX: {
      path: '/pages/device/index',
      name: '设备管理'
    },
    BIND: {
      path: '/pages/device/bind',
      name: '绑定设备'
    }
  }
};

/**
 * 导航到指定页面
 * @param {Object} route 路由对象
 * @param {Object} params 页面参数
 * @param {Object} options 导航选项
 */
export function navigateTo(route, params = {}, options = {}) {
  if (!route || !route.path) {
    console.error('无效的路由对象');
    return;
  }
  
  // 构建URL参数
  let url = route.path;
  const queryParams = [];
  
  for (const key in params) {
    if (params.hasOwnProperty(key) && params[key] !== undefined) {
      queryParams.push(`${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`);
    }
  }
  
  if (queryParams.length > 0) {
    url += '?' + queryParams.join('&');
  }
  
  // 执行导航
  uni.navigateTo({
    url,
    ...options
  });
}

/**
 * 重定向到指定页面（关闭当前页面）
 * @param {Object} route 路由对象
 * @param {Object} params 页面参数
 * @param {Object} options 导航选项
 */
export function redirectTo(route, params = {}, options = {}) {
  if (!route || !route.path) {
    console.error('无效的路由对象');
    return;
  }
  
  // 构建URL参数
  let url = route.path;
  const queryParams = [];
  
  for (const key in params) {
    if (params.hasOwnProperty(key) && params[key] !== undefined) {
      queryParams.push(`${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`);
    }
  }
  
  if (queryParams.length > 0) {
    url += '?' + queryParams.join('&');
  }
  
  // 执行重定向
  uni.redirectTo({
    url,
    ...options
  });
}

/**
 * 返回上一页或跳转到指定页面
 * @param {Object} defaultRoute 默认路由（当没有上一页时跳转）
 * @param {Object} params 页面参数
 * @param {Number} delta 返回的页面数
 */
export function navigateBack(defaultRoute = ROUTES.HOME, params = {}, delta = 1) {
  const pages = getCurrentPages();
  
  if (pages.length > 1) {
    // 有上一页，返回上一页
    uni.navigateBack({ delta });
  } else {
    // 没有上一页，跳转到默认页面
    navigateTo(defaultRoute, params);
  }
}

export default {
  ROUTES,
  navigateTo,
  redirectTo,
  navigateBack
};
