/**
 * HTTP请求服务
 * 结合了travle-uniapp和manager-web的优点
 */

// 配置基础URL
let baseUrl = '';

// 通过环境来判断
if (process.env.NODE_ENV === 'development') {
	// 开发环境使用本地API或Mock
	baseUrl = 'http://192.168.3.188:8002/xiaozhi';
} else {
	// 生产环境使用实际API地址
	baseUrl = 'http://xiaofei.ai-tulip.site/xiaozhi';
}

/**
 * HTTP请求函数
 * @param {String} url - 请求地址
 * @param {Object} data - 请求参数
 * @param {String} method - 请求方法
 * @param {Object} options - 其他选项
 * @returns {Promise} - 返回Promise对象
 */
const http = function(url, data = {}, method = 'GET', options = {}) {
	// 请求头
	const header = {
		'content-type': 'application/json; charset=utf-8',
		...(options.header || {})
	};
	
	// 添加token
	const token = uni.getStorageSync('token');
	if (token) {
		header.Authorization = 'Bearer ' + token;
	}
	
	// 处理URL参数
	let requestUrl = baseUrl + url;
	if (options && options.params) {
		const queryString = Object.keys(options.params)
			.map(key => `${encodeURIComponent(key)}=${encodeURIComponent(options.params[key])}`)
			.join('&');
		
		if (queryString) {
			requestUrl += (url.includes('?') ? '&' : '?') + queryString;
		}
	}
	
	// 返回Promise对象
	return new Promise((resolve, reject) => {
		uni.request({
			url: requestUrl,
			data,
			method,
			header,
			success: res => {
				// 处理HTTP状态码
				if (res.statusCode === 200) {
					// 处理业务状态码
					if (res.data.code === 0) {
						// 成功
						resolve(res.data.data);
					} else if (res.data.code === 401) {
						// 未授权，跳转到登录页
						uni.showToast({
							title: '登录已过期，请重新登录',
							icon: 'none'
						});
						// 清除token
						uni.removeStorageSync('token');
						// 跳转到登录页
						setTimeout(() => {
							uni.navigateTo({
								url: '/pages/login/index'
							});
						}, 1500);
						reject(res.data.msg);
					} else {
						// 其他业务错误
						uni.showToast({
							title: res.data.msg || '请求失败',
							icon: 'none'
						});
						reject(res.data.msg);
					}
				} else {
					// HTTP错误
					uni.showToast({
						title: `网络请求错误：${res.statusCode}`,
						icon: 'none'
					});
					reject(`网络请求错误：${res.statusCode}`);
				}
			},
			fail: err => {
				uni.showToast({
					title: '网络请求失败',
					icon: 'none'
				});
				reject(err);
			}
		});
	});
};

// 添加baseUrl属性
http.baseUrl = baseUrl;

export default http;

// 请求重试函数
export function reRequest(fn, maxRetries = 3, delay = 2000) {
	let retries = 0;
	
	function attempt() {
		return fn().catch(error => {
			if (retries < maxRetries) {
				retries++;
				uni.showToast({
					title: `正在重试 (${retries}/${maxRetries})`,
					icon: 'none'
				});
				return new Promise(resolve => {
					setTimeout(() => resolve(attempt()), delay);
				});
			}
			return Promise.reject(error);
		});
	}
	
	return attempt();
} 