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
                    style="margin-right: 20rpx;"
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
    
    <!-- 支付二维码弹窗 -->
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
        <view class="popup-title">扫码支付</view>
        <text class="qr-code-text">请使用微信扫码完成支付</text>
        <view class="qrcode-wrapper">
          <!-- 根据状态显示二维码或支付成功图标 -->
          <template v-if="paymentModal.payStatus === 'SUCCESS'">
            <view class="success-icon">
              <u-icon name="checkmark-circle" size="80" color="#07c160"></u-icon>
              <text class="success-text">支付成功</text>
            </view>
          </template>
          <template v-else>
            <u-qrcode 
              canvas-id="qrcode"
              :value="paymentModal.qrCodeUrl" 
              :options="{ margin: 10 }"
              :size="260"
            ></u-qrcode>
          </template>
        </view>
        <text class="qr-code-price">支付金额：¥{{ paymentModal.price }}</text>
        <view class="btn-group">
          <u-button 
            type="info" 
            @click="closePaymentModal" 
            size="medium"
            :text="paymentModal.payStatus === 'SUCCESS' ? '完成' : '取消'"
          ></u-button>
          <u-button 
            v-if="paymentModal.payStatus === 'SUCCESS'" 
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
        qrCodeUrl: '',
        orderNo: '',
        price: '0.00',
        payStatus: 'NOTPAY', // 支付状态: NOTPAY-未支付, SUCCESS-已支付
      },
      pollingTimer: null,
      pollingCount: 0, // 记录轮询次数
      maxPollingCount: 60 // 最大轮询次数 (60次 * 3秒 = 3分钟)
    }
  },
  onReady() {
    this.$refs.uForm.setRules(this.rules);
  },
  onUnload() {
    if (this.audioContext) this.audioContext.destroy();
    this.stopPolling();
  },
  methods: {
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
    },
    
    handleFileSelect(file) {
      if (!file) return;
      const maxSize = 5 * 1024 * 1024; // 5MB
      if (file.size > maxSize) {
        return uni.showToast({ title: '文件大小不能超过5MB', icon: 'none' });
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
        
        // 创建支付订单
        paymentApi.createCloneOrder(this.form, this.audioFile)
          .then(res => {
            uni.hideLoading();
            console.log('支付订单创建成功:', res);
            
            if (res.code === 0 && res.data) {
              // 显示支付二维码
              this.paymentModal = {
                show: true,
                qrCodeUrl: res.data.codeUrl,
                orderNo: res.data.orderNo,
                price: res.data.price || '1.00',
                payStatus: 'NOTPAY'
              };
              
              // 开始轮询支付状态
              this.startPolling();
            } else {
              uni.showToast({
                title: res.msg || '创建订单失败',
                icon: 'none'
              });
            }
          })
          .catch(err => {
            uni.hideLoading();
            console.error('创建订单失败:', err);
            uni.showToast({
              title: err.msg || '网络请求失败',
              icon: 'none',
              duration: 2000
            });
          })
          .finally(() => {
            this.loading = false;
          });
      });
    },
    
    closePaymentModal() {
      this.paymentModal.show = false;
      this.stopPolling();
    },
    
    startPolling() {
      this.stopPolling();
      this.pollingCount = 0;
      
      // 轮询支付状态
      this.pollingTimer = setInterval(() => {
        this.checkPaymentStatus();
      }, 3000); // 每3秒查询一次
    },
    
    stopPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer);
        this.pollingTimer = null;
      }
    },
    
    checkPaymentStatus() {
      if (!this.paymentModal.orderNo) return;
      
      this.pollingCount++;
      
      // 超出最大轮询次数时停止
      if (this.pollingCount > this.maxPollingCount) {
        console.log('超出轮询次数限制，停止轮询');
        this.stopPolling();
        return;
      }
      
      paymentApi.getOrderStatus(this.paymentModal.orderNo)
        .then(res => {
          if (res.code === 0) {
            const status = res.data;
            console.log('订单状态:', status);
            
            // 支付成功
            if (status === 'SUCCESS') {
              this.stopPolling();
              this.paymentModal.payStatus = 'SUCCESS';
              
              uni.showToast({
                title: '支付成功！克隆任务已开始',
                icon: 'success',
                duration: 2000
              });
            }
          }
        })
        .catch(err => {
          console.error('查询订单状态失败:', err);
        });
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
}

.audio-info {
  display: flex;
  align-items: center;
  flex: 1;
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

.qrcode-wrapper {
  width: 300rpx;
  height: 300rpx;
  padding: 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 8rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  margin: 20rpx 0;
}

.success-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
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
