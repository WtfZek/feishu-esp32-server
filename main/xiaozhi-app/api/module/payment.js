import http from '@/api/http';
import { API } from '../api';

const paymentApi = {
  /**
   * 创建语音克隆支付订单
   * @param {Object} data - 包含克隆信息的对象 {name, languages, remark}
   * @param {Object} file - uni.chooseFile 返回的临时文件对象
   */
  createCloneOrder(data, file) {
    // 根据平台确定支付类型
    let tradeType = 'H5'; // 默认使用H5支付，不再使用NATIVE
    
    // #ifdef MP-WEIXIN
    tradeType = 'JSAPI';
    // #endif
    
    // #ifdef H5
    tradeType = 'H5';
    // #endif
    
    // #ifdef APP-PLUS
    tradeType = 'APP';
    // #endif
    
    console.log('当前使用的支付类型:', tradeType); // 添加日志，帮助调试
    
    // 构建表单数据
    const formData = {
      name: data.name,
      languages: data.languages,
      remark: data.remark,
      // 添加固定的交易参数
      productCode: '11111111111', // 固定商品编码
      tradeType: tradeType // 根据平台设置支付方式
    };
    
    // 如果是JSAPI支付，确保传递openid
    if (tradeType === 'JSAPI' && data.openid) {
      formData.openid = data.openid;
      console.log('传递openid到后端:', data.openid); // 添加日志，帮助调试
    }
    
    // 处理文件路径
    let filePath = file.path || file.tempFilePath;
    
    // 检查是否为Android 10+系统下的content://路径
    const isContentUri = filePath && filePath.startsWith('content://');
    console.log('文件路径:', filePath, '是否为content路径:', isContentUri);
    
    // #ifdef APP-PLUS
    // 如果是content://路径，且有uri属性，优先使用uri
    if (isContentUri && file.uri) {
      filePath = file.uri;
      console.log('使用文件uri上传:', filePath);
    }
    // #endif
    
    // uni-app 的上传文件 API
    return new Promise((resolve, reject) => {
      // #ifdef APP-PLUS
      // 如果是Android 10+系统下的content://路径，使用files参数
      if (isContentUri) {
        console.log('使用files参数上传content://路径文件');
        
        const uploadTask = uni.uploadFile({
          url: http.baseUrl + API.PAY.CREATE_CLONE_ORDER,
          files: [{
            name: 'voiceFile',
            uri: filePath
          }],
          formData: formData,
          header: {
            'Authorization': 'Bearer ' + uni.getStorageSync('token')
          },
          success: (uploadRes) => {
            try {
              // uni.uploadFile 返回的data是字符串，需要解析
              const res = JSON.parse(uploadRes.data);
              if (res.code === 0) {
                resolve(res);
              } else {
                console.error('创建克隆订单失败:', res);
                const errorMsg = res.msg || '创建支付订单失败';
                uni.showToast({
                  title: errorMsg,
                  icon: 'none',
                  duration: 3000
                });
                reject(res);
              }
            } catch(e) {
              console.error('解析服务器响应失败:', e, uploadRes);
              reject({ msg: '服务器响应解析失败', error: e, originalResponse: uploadRes });
            }
          },
          fail: (err) => {
            console.error('文件上传失败:', err);
            uni.showToast({
              title: '网络请求失败，请检查网络连接',
              icon: 'none',
              duration: 3000
            });
            reject(err);
          }
        });
        
        // 监听上传进度
        uploadTask.onProgressUpdate((res) => {
          console.log('上传进度:', res.progress);
        });
        
        return;
      }
      // #endif
      
      // 常规文件上传
      uni.uploadFile({
        url: http.baseUrl + API.PAY.CREATE_CLONE_ORDER,
        filePath: filePath,
        name: 'voiceFile', // 与后端 @RequestPart("voiceFile") 对应
        formData: formData,
        header: {
          'Authorization': 'Bearer ' + uni.getStorageSync('token') 
        },
        success: (uploadRes) => {
          try {
            // uni.uploadFile 返回的data是字符串，需要解析
            const res = JSON.parse(uploadRes.data);
            if (res.code === 0) {
              resolve(res);
            } else {
              console.error('创建克隆订单失败:', res);
              const errorMsg = res.msg || '创建支付订单失败';
              uni.showToast({
                title: errorMsg,
                icon: 'none',
                duration: 3000
              });
              reject(res);
            }
          } catch(e) {
            console.error('解析服务器响应失败:', e, uploadRes);
            reject({ msg: '服务器响应解析失败', error: e, originalResponse: uploadRes });
          }
        },
        fail: (err) => {
          console.error('文件上传失败:', err);
          uni.showToast({
            title: '网络请求失败，请检查网络连接',
            icon: 'none',
            duration: 3000
          });
          reject(err);
        }
      });
    });
  },

  /**
   * 查询订单状态
   * @param {string} orderNo - 订单号
   */
  getOrderStatus(orderNo) {
    if (!orderNo) {
      return Promise.reject({ msg: "订单号不能为空" });
    }
    const url = API.PAY.GET_ORDER_STATUS.replace('{orderNo}', orderNo);
    return http(url);
  },
  
  /**
   * 检查H5支付后的订单状态
   * 用于H5支付完成后跳转回应用时检查支付结果
   */
  checkH5PaymentResult() {
    // #ifdef H5
    const pendingOrderNo = uni.getStorageSync('pendingOrderNo');
    if (pendingOrderNo) {
      return this.getOrderStatus(pendingOrderNo)
        .then(res => {
          if (res.code === 0 && res.data === 'SUCCESS') {
            // 支付成功，清除订单号
            uni.removeStorageSync('pendingOrderNo');
            return { success: true, orderNo: pendingOrderNo };
          }
          return { success: false, orderNo: pendingOrderNo };
        })
        .catch(err => {
          console.error('查询订单状态失败:', err);
          return { success: false, error: err };
        });
    }
    return Promise.resolve({ success: false, reason: 'no_pending_order' });
    // #endif
    
    // 非H5环境直接返回
    return Promise.resolve({ success: false, reason: 'not_h5_environment' });
  }
};

export default paymentApi; 