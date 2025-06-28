<template>
  <view class="container">
    
    <view class="content">
      <view class="form-card">
        <view class="form-title">创建克隆音色</view>
        
        <u-form
          :model="form"
          ref="uForm"
          errorType="message"
          labelPosition="top"
          labelWidth="100%"
          :rules="rules"
        >
          <!-- 音色名称 -->
          <u-form-item
            label="音色名称"
            prop="name"
            :required="true"
            labelStyle="font-weight: bold;"
          >
            <u-input
              v-model="form.name"
              placeholder="请输入音色名称"
              border="true"
              :clearable="true"
              :customStyle="{
                backgroundColor: '#f5f7fa',
                borderRadius: '8rpx'
              }"
            ></u-input>
          </u-form-item>
          
          <!-- 语言 -->
          <u-form-item
            label="语言"
            prop="languages"
            :required="true"
            labelStyle="font-weight: bold;"
          >
            <u-radio-group v-model="form.languages">
              <u-radio label="中文" name="zh"></u-radio>
              <u-radio label="英文" name="en" style="margin-left: 30rpx;"></u-radio>
            </u-radio-group>
          </u-form-item>
          
          <!-- 音频文件上传 -->
          <u-form-item
            label="上传音频文件"
            prop="voiceFile"
            :required="true"
            labelStyle="font-weight: bold;"
          >
            <view class="upload-container">
              <view 
                class="upload-box" 
                @click="chooseFile" 
                v-if="!audioFile"
              >
                <u-icon name="plus" size="40rpx" color="#909399"></u-icon>
                <text class="upload-text">点击上传音频</text>
                <text class="upload-tips">支持wav、mp3、m4a格式，最大5MB</text>
              </view>
              
              <view class="audio-preview" v-else>
                <view class="audio-info">
                  <u-icon 
                    :name="isPlaying ? 'pause-circle' : 'play-circle'" 
                    size="24" 
                    color="#5778ff" 
                    @click="playAudio"
                    style="margin-right: 20rpx; flex-shrink: 0;"
                  ></u-icon>
                  <text class="audio-name">{{ audioFile.name }}</text>
                </view>
                <view class="audio-actions">
                  <u-icon 
                    name="trash" 
                    size="24" 
                    color="#fa3534" 
                    @click="removeFile"
                  ></u-icon>
                </view>
              </view>
            </view>
          </u-form-item>
          
          <!-- 备注 -->
          <u-form-item
            label="备注"
            prop="remark"
            labelStyle="font-weight: bold;"
          >
            <u-input
              v-model="form.remark"
              type="textarea"
              placeholder="请输入备注信息"
              border="true"
              :clearable="true"
              height="100"
              :customStyle="{
                backgroundColor: '#f5f7fa',
                borderRadius: '8rpx'
              }"
            ></u-input>
          </u-form-item>
        </u-form>
        
        <!-- 提交按钮 -->
        <view class="submit-btn-wrapper">
          <u-button
            type="primary"
            text="开始克隆"
            :loading="loading"
            @click="submitForm"
          ></u-button>
        </view>
      </view>
    </view>
    
    <!-- 支付成功弹窗 -->
    <u-popup
      :show="paymentModal.show"
      mode="center"
      @close="closePaymentModal"
      closeable
      :closeOnClickOverlay="true"
      borderRadius="16"
      width="80%"
    >
      <view class="qr-code-container">
        <view class="popup-title">支付结果</view>
        <view class="success-icon">
          <u-icon name="checkmark-circle" size="80" color="#07c160"></u-icon>
          <text class="success-text">支付成功</text>
        </view>
        <text class="qr-code-price">支付金额：¥{{ paymentModal.price }}</text>
        <view class="btn-group">
          <u-button 
            type="info" 
            @click="closePaymentModal" 
            size="medium"
            text="完成"
          ></u-button>
          <u-button 
            type="primary" 
            @click="gotoList" 
            size="medium"
            style="margin-left: 20rpx;" 
            text="查看我的音色"
          ></u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import paymentApi from '@/api/module/payment';
import wechatApi from '@/api/module/wechat';
import Router from '@/utils/router';

export default {
  data() {
    return {
      form: {
        name: '',
        languages: 'zh',
        remark: '',
        voiceFile: null
      },
      audioFile: null,
      loading: false,
      rules: {
        name: [{ required: true, message: '请输入音色名称', trigger: ['blur', 'change'] }],
        languages: [{ required: true, message: '请选择语言', trigger: 'change' }],
        voiceFile: [{ 
          required: true, 
          validator: (rule, value, callback) => {
            if (this.audioFile) callback();
            else callback(new Error('请上传音频文件'));
          } 
        }]
      },
      isPlaying: false,
      audioContext: null,
      paymentModal: {
        show: false,
        orderNo: '',
        price: '0.00',
      },
      openid: ''
    }
  },
  onReady() {
    this.$refs.uForm.setRules(this.rules);
  },
  onLoad() {
    // #ifdef H5
    // 检查是否有H5支付结果需要处理
    paymentApi.checkH5PaymentResult().then(result => {
      if (result.success) {
        this.handlePaymentSuccess();
      }
    });
    // #endif
    
    // #ifdef MP-WEIXIN
    // 微信小程序环境下获取openid
    this.getWxOpenid();
    // #endif
  },
  onUnload() {
    if (this.audioContext) this.audioContext.destroy();
  },
  methods: {
    // 获取微信openid
    getWxOpenid() {
      // 先从本地存储获取
      const openid = uni.getStorageSync('wx_openid');
      if (openid) {
        this.openid = openid;
        console.log('从缓存获取openid:', this.openid);
        return Promise.resolve(openid);
      }
      
      // 如果本地没有，则通过登录获取
      return new Promise((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: (loginRes) => {
            if (loginRes.code) {
              console.log('微信登录成功，获取code:', loginRes.code);
              // 将code发送到后端换取openid
              wechatApi.getOpenid(loginRes.code)
                .then(res => {
                  if (res) {
                    this.openid = res;
                    // 存储到本地
                    uni.setStorageSync('wx_openid', this.openid);
                    console.log('获取openid成功:', this.openid);
                    resolve(this.openid);
                  } else {
                    console.error('获取openid失败:', res);
                    uni.showToast({
                      title: '获取用户信息失败',
                      icon: 'none'
                    });
                    reject(new Error('获取openid失败'));
                  }
                })
                .catch(err => {
                  console.error('获取openid请求失败:', err);
                  uni.showToast({
                    title: '网络请求失败',
                    icon: 'none'
                  });
                  reject(err);
                });
            } else {
              console.error('微信登录失败:', loginRes);
              uni.showToast({
                title: '微信登录失败',
                icon: 'none'
              });
              reject(new Error('微信登录失败'));
            }
          },
          fail: (err) => {
            console.error('微信登录失败:', err);
            uni.showToast({
              title: '微信登录失败',
              icon: 'none'
            });
            reject(err);
          }
        });
      });
    },
    
    chooseFile() {
      // 微信小程序环境
      // #ifdef MP-WEIXIN
      uni.chooseMessageFile({
        count: 1,
        type: 'file',
        extension: ['wav', 'mp3', 'm4a'],
        success: res => this.handleFileSelect(res.tempFiles[0]),
        fail: err => uni.showToast({ title: '选择文件失败', icon: 'none' })
      });
      // #endif
      
      // H5环境
      // #ifdef H5
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.wav,.mp3,.m4a';
      input.onchange = e => this.handleFileSelect(e.target.files[0]);
      input.click();
      // #endif
      
      // APP环境
      // #ifdef APP-PLUS
      if (typeof plus !== 'undefined') {
        try {
          // 使用plus.io.chooseFile选择文件
          plus.io.chooseFile({
            title: '选择音频文件',
            filetypes: ['mp3', 'wav', 'm4a'], // 允许的文件类型
            multiple: false, // 不允许多选
          }, (res) => {
            console.log('选择文件结果:', res);
            
            // 检查是否有文件被选择
            if (res && res.files && res.files.length > 0) {
              const filePath = res.files[0];
              console.log('选择的文件路径:', filePath);
              
              // 检查文件类型是否符合要求
              const lowerPath = filePath.toLowerCase();
              const isAudioFile = lowerPath.endsWith('.wav') || 
                                 lowerPath.endsWith('.mp3') || 
                                 lowerPath.endsWith('.m4a');
              
              if (!isAudioFile) {
                uni.showToast({
                  title: '请选择.wav、.mp3或.m4a格式的音频文件',
                  icon: 'none',
                  duration: 3000
                });
                return;
              }
              
              // 处理Android 10+系统下的content://路径
              if (filePath.startsWith('content://')) {
                console.log('检测到content://路径，直接使用该路径上传');
                
                // 从路径中提取文件名
                const fileName = filePath.substring(filePath.lastIndexOf('/') + 1) || '音频文件.mp3';
                
                // 检查文件扩展名
                if (!this.checkAudioFileExtension(fileName)) {
                  uni.showToast({
                    title: '请选择.wav、.mp3或.m4a格式的音频文件',
                    icon: 'none',
                    duration: 3000
                  });
                  return;
                }
                
                // 处理选择的文件
                this.handleFileSelect({
                  name: fileName,
                  path: filePath,
                  tempFilePath: filePath,
                  uri: filePath // 保存uri，用于上传
                });
              } else {
                // 处理普通文件路径
                const fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
                
                // 检查文件扩展名
                if (!this.checkAudioFileExtension(fileName)) {
                  uni.showToast({
                    title: '请选择.wav、.mp3或.m4a格式的音频文件',
                    icon: 'none',
                    duration: 3000
                  });
                  return;
                }
                
                // 处理选择的文件
                this.handleFileSelect({
                  name: fileName,
                  path: filePath,
                  tempFilePath: filePath
                });
              }
            } else {
              console.log('未选择文件');
            }
          }, (err) => {
            console.error('选择文件失败:', err);
            this.useChooseImage();
          });
        } catch (e) {
          console.error('调用plus.io.chooseFile失败:', e);
          this.useChooseImage();
        }
      } else {
        console.log('plus对象不可用，使用chooseImage');
        this.useChooseImage();
      }
      // #endif
    },
    
    // 检查文件是否为音频文件
    checkAudioFileExtension(fileName) {
      if (!fileName) return false;
      const lowerName = fileName.toLowerCase();
      return lowerName.endsWith('.wav') || 
             lowerName.endsWith('.mp3') || 
             lowerName.endsWith('.m4a');
    },
    
    // APP环境下使用chooseImage作为备选方案
    useChooseImage() {
      uni.showToast({
        title: '请从相册中选择音频文件',
        icon: 'none',
        duration: 2000
      });
      
      setTimeout(() => {
        uni.chooseImage({
          count: 1,
          sourceType: ['album'],
          success: res => {
            if (res.tempFilePaths && res.tempFilePaths.length > 0) {
              this.handleFileSelect({
                name: '音频文件.mp3',
                path: res.tempFilePaths[0],
                size: res.tempFiles ? res.tempFiles[0].size : 0,
                tempFilePath: res.tempFilePaths[0]
              });
            }
          },
          fail: err => {
            console.error('选择文件失败:', err);
            uni.showToast({ title: '选择文件失败', icon: 'none' });
          }
        });
      }, 1000);
    },
    
    // 处理文件选择
    handleFileSelect(file) {
      if (!file) return;
      const maxSize = 5 * 1024 * 1024; // 5MB
      if (file.size > maxSize) {
        return uni.showToast({ title: '文件大小不能超过5MB', icon: 'none' });
      }
      
      // 检查文件类型
      if (file.name && !this.checkAudioFileExtension(file.name)) {
        return uni.showToast({ 
          title: '请选择.wav、.mp3或.m4a格式的音频文件', 
          icon: 'none',
          duration: 3000
        });
      }
      
      // 确保文件对象包含基本信息
      this.audioFile = {
        ...file,
        name: file.name || '音频文件.wav',
        path: file.path || file.tempFilePath || (file.file && URL.createObjectURL(file.file))
      };
      
      this.form.voiceFile = this.audioFile;
      this.$refs.uForm.validateField('voiceFile');
    },
    
    removeFile() {
      if (this.isPlaying && this.audioContext) {
        this.audioContext.stop();
        this.isPlaying = false;
      }
      this.audioFile = null;
      this.form.voiceFile = null;
    },
    
    submitForm() {
      if (!this.$refs.uForm) {
        return uni.showToast({ title: '表单未初始化', icon: 'none' });
      }
      
      this.$refs.uForm.validate().then(valid => {
        if (!valid) return;
        
        if (!this.audioFile) {
          return uni.showToast({ title: '请上传音频文件', icon: 'none' });
        }
        
        this.loading = true;
        uni.showLoading({ title: '正在创建订单...' });
        
        // 构建支付参数
        const paymentData = {
          ...this.form
        };
        
        // 处理不同平台的支付方式
        // #ifdef MP-WEIXIN
        // 小程序环境下，先获取openid再创建支付订单
        const openid = uni.getStorageSync('wx_openid');
        if (openid) {
          // 如果已经有缓存的openid，直接使用
          paymentData.openid = openid;
          paymentData.tradeType = 'JSAPI'; // 指定为小程序支付
          console.log('使用缓存的openid创建订单:', openid);
          this.createPayOrder(paymentData);
        } else {
          // 如果没有缓存的openid，先获取
          this.getWxOpenid()
            .then(openid => {
              paymentData.openid = openid;
              paymentData.tradeType = 'JSAPI'; // 指定为小程序支付
              return this.createPayOrder(paymentData);
            })
            .catch(err => {
              uni.hideLoading();
              this.loading = false;
              console.error('获取openid失败:', err);
              uni.showToast({
                title: '获取用户信息失败，请重试',
                icon: 'none',
                duration: 2000
              });
            });
        }
        // #endif
        
        // #ifndef MP-WEIXIN
        // 非小程序环境直接创建支付订单
        this.createPayOrder(paymentData);
        // #endif
      });
    },
    
    // 创建支付订单
    createPayOrder(paymentData) {
      // 创建支付订单
      return paymentApi.createCloneOrder(paymentData, this.audioFile)
        .then(res => {
          uni.hideLoading();
          console.log('支付订单创建成功:', res);
          
          if (res && res.code === 0 && res.data) {
            // 根据不同平台调用不同的支付方式
            this.handlePayment(res.data);
          } else {
            uni.showToast({
              title: res && res.msg || '创建订单失败',
              icon: 'none'
            });
          }
        })
        .catch(err => {
          uni.hideLoading();
          console.error('创建订单失败:', err);
          uni.showToast({
            title: err && err.msg || '网络请求失败',
            icon: 'none',
            duration: 2000
          });
        })
        .finally(() => {
          this.loading = false;
          // 确保加载提示被隐藏
          setTimeout(() => {
            uni.hideLoading();
          }, 100);
        });
    },
    
    // 处理不同平台的支付方式
    handlePayment(payData) {
      // 保存订单信息
      this.paymentModal.orderNo = payData.orderNo;
      this.paymentModal.price = payData.price || '1.00';
      
      // #ifdef MP-WEIXIN
      this.handleWechatMiniProgramPay(payData);
      // #endif
      
      // #ifdef H5
      this.handleH5Pay(payData);
      // #endif
      
      // #ifdef APP-PLUS
      this.handleAppPay(payData);
      // #endif
    },
    
    // 微信小程序支付
    handleWechatMiniProgramPay(payData) {
      if (!payData.miniAppParams) {
        return uni.showToast({
          title: '支付参数错误',
          icon: 'none'
        });
      }
      
      const params = payData.miniAppParams;
      uni.requestPayment({
        provider: 'wxpay',
        timeStamp: params.timeStamp,
        nonceStr: params.nonceStr,
        package: params.packageValue,
        signType: params.signType,
        paySign: params.paySign,
        success: res => {
          console.log('支付成功', res);
          this.handlePaymentSuccess();
        },
        fail: err => {
          console.error('支付失败', err);
          uni.showToast({
            title: '支付已取消或失败',
            icon: 'none'
          });
        }
      });
    },
    
    // H5支付
    handleH5Pay(payData) {
      if (!payData.h5Url) {
        return uni.showToast({
          title: '支付链接错误',
          icon: 'none'
        });
      }
      
      // 记录订单号，用于支付完成后查询
      uni.setStorageSync('pendingOrderNo', payData.orderNo);
      
      // 打开微信支付H5页面
      window.location.href = payData.h5Url;
      
      // 监听页面返回事件，检查支付状态
      window.addEventListener('pageshow', this.checkH5PaymentStatus);
    },
    
    // 检查H5支付状态
    checkH5PaymentStatus() {
      const pendingOrderNo = uni.getStorageSync('pendingOrderNo');
      if (pendingOrderNo) {
        paymentApi.getOrderStatus(pendingOrderNo)
          .then(res => {
            if (res.code === 0 && res.data === 'SUCCESS') {
              // 支付成功
              this.handlePaymentSuccess();
              // 清除订单号
              uni.removeStorageSync('pendingOrderNo');
            }
          })
          .catch(err => {
            console.error('查询订单状态失败:', err);
          });
      }
    },
    
    // APP支付
    handleAppPay(payData) {
      if (!payData.appUrl) {
        return uni.showToast({
          title: '支付参数错误',
          icon: 'none'
        });
      }
      
      try {
        const appPayParams = JSON.parse(payData.appUrl);
        uni.requestPayment({
          provider: 'wxpay',
          orderInfo: appPayParams, // APP支付所需参数
          success: res => {
            console.log('支付成功', res);
            this.handlePaymentSuccess();
          },
          fail: err => {
            console.error('支付失败', err);
            uni.showToast({
              title: '支付已取消或失败',
              icon: 'none'
            });
          }
        });
      } catch (e) {
        console.error('解析支付参数失败', e);
        uni.showToast({
          title: '支付参数错误',
          icon: 'none'
        });
      }
    },
    
    // 处理支付成功
    handlePaymentSuccess() {
      // 显示支付成功弹窗
      this.paymentModal.show = true;
      
      uni.showToast({
        title: '支付成功！克隆任务已开始',
        icon: 'success',
        duration: 2000
      });
    },
    
    closePaymentModal() {
      this.paymentModal.show = false;
    },
    
    gotoList() {
      this.closePaymentModal();
      // 跳转到音色列表页面
      Router.navigateTo(Router.ROUTES.VOICE.INDEX);
    },
    
    playAudio() {
      if (!this.audioFile || !this.audioFile.path) {
        return uni.showToast({ title: '音频播放失败', icon: 'none' });
      }
      
      // 初始化音频上下文
      if (!this.audioContext) {
        this.audioContext = uni.createInnerAudioContext();
        
        this.audioContext.onPlay(() => {
          this.isPlaying = true;
        });
        
        this.audioContext.onStop(() => {
          this.isPlaying = false;
        });
        
        this.audioContext.onEnded(() => {
          this.isPlaying = false;
        });
        
        this.audioContext.onError(() => {
          this.isPlaying = false;
          uni.showToast({ title: '音频播放失败', icon: 'none' });
        });
      }
      
      // 切换播放状态
      if (this.isPlaying) {
        this.audioContext.stop();
      } else {
        this.audioContext.src = this.audioFile.path;
        this.audioContext.play();
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.content {
  padding: 30rpx;
}

.form-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 40rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.form-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 40rpx;
  text-align: center;
}

.upload-container {
  width: 100%;
  margin-top: 10rpx;
}

.upload-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200rpx;
  border: 2rpx dashed #dcdfe6;
  border-radius: 8rpx;
  background-color: #fafafa;
  cursor: pointer;
}

.upload-text {
  font-size: 28rpx;
  color: #606266;
  margin-top: 20rpx;
}

.upload-tips {
  font-size: 24rpx;
  color: #909399;
  margin-top: 10rpx;
}

.audio-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  border: 2rpx solid #e4e7ed;
  border-radius: 8rpx;
  background-color: #f5f7fa;
  width: 100%;
  box-sizing: border-box;
}

.audio-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.audio-name {
  margin-left: 20rpx;
  font-size: 28rpx;
  color: #303133;
  word-break: break-all;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 400rpx;
}

.audio-actions {
  margin-left: 20rpx;
  padding: 10rpx;
}

.submit-btn-wrapper {
  margin-top: 60rpx;
  padding: 0 20rpx;
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 20rpx 30rpx;
}

.popup-title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20rpx;
  color: #303133;
  width: 100%;
}

.qr-code-text {
  font-size: 28rpx;
  color: #606266;
  margin-bottom: 20rpx;
}

.success-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20rpx 0;
}

.success-text {
  font-size: 28rpx;
  color: #07c160;
  margin-top: 20rpx;
  font-weight: bold;
}

.qr-code-price {
  font-size: 32rpx;
  font-weight: bold;
  color: #fa3534;
  margin-top: 30rpx;
}

.btn-group {
  display: flex;
  margin-top: 30rpx;
}
</style>
