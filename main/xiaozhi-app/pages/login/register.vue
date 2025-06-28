<template>
  <view class="register-page">
    <!-- 顶部图片 -->
    <view class="login-header">
      <image class="login-logo" src="/static/logo.png" mode="aspectFit"></image>
      <text class="login-title">飞鼠智控终端</text>
    </view>
    
    <view class="register-content">
      <view class="form-container">
        <view class="login-form__title">
          <text class="login-form__welcome">欢迎注册</text>
          <text class="login-form__subtitle">WELCOME TO REGISTER</text>
        </view>
        
        <u-form :model="form" ref="uForm" :rules="rules">
          <!-- 用户名/手机号切换 -->
          <view v-if="!enableMobileRegister">
            <u-form-item label="用户名" prop="username" labelWidth="160rpx">
              <u-input 
                v-model="form.username" 
                placeholder="请输入用户名"
                prefixIcon="account"
                border="bottom"
                clearable
              ></u-input>
            </u-form-item>
          </view>
          
          <!-- 手机号注册 -->
          <view v-else>
            <u-form-item label="手机号" prop="mobile" labelWidth="160rpx">
              <u-input 
                v-model="form.mobile" 
                type="number"
                placeholder="请输入手机号"
                prefixIcon="phone"
                border="bottom"
                clearable
              ></u-input>
            </u-form-item>
            
            <u-form-item label="验证码" prop="mobileCaptcha" labelWidth="160rpx">
              <view class="code-input">
                <u-input 
                  v-model="form.mobileCaptcha" 
                  placeholder="请输入验证码"
                  prefixIcon="checkmark"
                  border="bottom"
                  clearable
                ></u-input>
                <u-button 
                  type="primary" 
                  size="mini" 
                  :text="countdown > 0 ? `${countdown}秒` : '获取验证码'"
                  :disabled="countdown > 0 || !validateMobileInput(form.mobile)"
                  @click="sendMobileCaptcha"
                ></u-button>
              </view>
            </u-form-item>
          </view>
          
          <u-form-item v-if="renderPasswordInput" label="密码" prop="password" labelWidth="160rpx">
            <view class="password-input-container">
              <u-input 
                ref="passwordInput"
                v-model="form.password" 
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                prefixIcon="lock"
                border="bottom"
                :password="!showPassword"
              ></u-input>
              <view class="eye-icon" @tap="togglePasswordVisibility">
                <u-icon name="eye" size="44rpx" :color="showPassword ? '#979db1' : '#5778ff'"></u-icon>
              </view>
            </view>
          </u-form-item>
          <u-form-item v-if="!renderPasswordInput" label="密码" prop="password" labelWidth="160rpx">
            <view class="password-input-container">
              <u-input 
                ref="passwordInput"
                v-model="form.password" 
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                prefixIcon="lock"
                border="bottom"
                :password="!showPassword"
              ></u-input>
              <view class="eye-icon" @tap="togglePasswordVisibility">
                <u-icon name="eye" size="44rpx" :color="showPassword ? '#979db1' : '#5778ff'"></u-icon>
              </view>
            </view>
          </u-form-item>
          
          <u-form-item v-if="renderConfirmInput" label="确认密码" prop="confirmPassword" labelWidth="160rpx">
            <view class="password-input-container">
              <u-input 
                ref="confirmPasswordInput"
                v-model="form.confirmPassword" 
                :type="showPassword ? 'text' : 'password'"
                placeholder="请再次输入密码"
                prefixIcon="lock"
                border="bottom"
                :password="!showPassword"
              ></u-input>
              <view class="eye-icon" @tap="togglePasswordVisibility">
                <u-icon name="eye" size="44rpx" :color="showPassword ? '#979db1' : '#5778ff'"></u-icon>
              </view>
            </view>
          </u-form-item>
          <u-form-item v-if="!renderConfirmInput" label="确认密码" prop="confirmPassword" labelWidth="160rpx">
            <view class="password-input-container">
              <u-input 
                ref="confirmPasswordInput"
                v-model="form.confirmPassword" 
                :type="showPassword ? 'text' : 'password'"
                placeholder="请再次输入密码"
                prefixIcon="lock"
                border="bottom"
                :password="!showPassword"
              ></u-input>
              <view class="eye-icon" @tap="togglePasswordVisibility">
                <u-icon name="eye" size="44rpx" :color="showPassword ? '#979db1' : '#5778ff'"></u-icon>
              </view>
            </view>
          </u-form-item>
          
          <!-- 图形验证码 -->
          <!-- <u-form-item label="验证码" prop="captcha" labelWidth="160rpx">
            <view class="code-input">
              <u-input 
                v-model="form.captcha" 
                placeholder="请输入验证码"
                prefixIcon="checkmark"
                border="bottom"
                clearable
              ></u-input>
              <image 
                class="captcha-image" 
                :src="captchaUrl" 
                @click="fetchCaptcha" 
                v-if="captchaUrl"
              ></image>
            </view>
          </u-form-item> -->
        </u-form>
        
        <view class="form-actions">
          <u-button type="primary" text="注册" @click="register"></u-button>
        </view>
        
        <view class="login-link">
          已有账号？<text class="link-text" @click="goToLogin">去登录</text>
        </view>
        
        <!-- 用户协议 -->
        <view class="login-form__agreement">
          <text class="agreement-text">注册即同意</text>
          <text class="agreement-link">《用户协议》</text>
          <text class="agreement-text">和</text>
          <text class="agreement-link">《隐私政策》</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import userApi from '@/api/module/user';
import { validateMobile } from '@/utils/validate';

export default {
  data() {
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        mobile: '',
        // captcha: '',
        captchaId: '',
        mobileCaptcha: '',
        areaCode: '+86'
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.form.password) {
                callback(new Error('两次输入密码不一致'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ],
        mobile: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (!validateMobile(value)) {
                callback(new Error('请输入正确的手机号'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ],
        // captcha: [
        //   { required: true, message: '请输入验证码', trigger: 'blur' }
        // ],
        mobileCaptcha: [
          { required: true, message: '请输入手机验证码', trigger: 'blur' }
        ]
      },
      // captchaUrl: '',
      showPassword: false,
      enableMobileRegister: false,
      countdown: 0,
      timer: null,
      // 添加来源页面记录
      fromLogin: false,
      renderPasswordInput: true,
      renderConfirmInput: true
    }
  },
  onLoad(options) {
    this.getPubConfig();
    // this.fetchCaptcha();
    
    // 记录是否从登录页进入
    this.fromLogin = options.from === 'login';
    console.log('是否从登录页进入:', this.fromLogin);
  },
  methods: {
    // 获取验证码
    // async fetchCaptcha() {
    //   this.form.captchaId = this.generateUUID();
    //   try {
    //     // 获取验证码URL
    //     this.captchaUrl = await userApi.getCaptcha(this.form.captchaId);
    //   } catch (error) {
    //     console.error('获取验证码失败', error);
    //     uni.showToast({
    //       title: '验证码加载失败，点击刷新',
    //       icon: 'none'
    //     });
    //   }
    // },

    // 获取公共配置
    async getPubConfig() {
      try {
        const config = await userApi.getPubConfig();
        this.enableMobileRegister = config.enableMobileRegister || false;
      } catch (error) {
        console.error('获取公共配置失败', error);
      }
    },

    // 验证手机号
    validateMobileInput(mobile) {
      return validateMobile(mobile);
    },

    // 发送手机验证码
    async sendMobileCaptcha() {
      if (!this.validateMobileInput(this.form.mobile)) {
        uni.showToast({
          title: '请输入正确的手机号码',
          icon: 'none'
        });
        return;
      }

      // if (!this.form.captcha) {
      //   uni.showToast({
      //     title: '请输入图形验证码',
      //     icon: 'none'
      //   });
      //   this.fetchCaptcha();
      //   return;
      // }

      // 清除可能存在的旧定时器
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }

      // 开始倒计时
      this.countdown = 60;
      this.timer = setInterval(() => {
        if (this.countdown > 0) {
          this.countdown--;
        } else {
          clearInterval(this.timer);
          this.timer = null;
        }
      }, 1000);

      // 调用发送验证码接口
      try {
        await userApi.sendSmsVerification({
          phone: this.form.areaCode + this.form.mobile,
          captcha: this.form.captcha,
          captchaId: this.form.captchaId
        });
        
        uni.showToast({
          title: '验证码发送成功',
          icon: 'success'
        });
      } catch (error) {
        uni.showToast({
          title: error || '验证码发送失败',
          icon: 'none'
        });
        this.countdown = 0;
        // this.fetchCaptcha();
      }
    },

    // 注册
    register() {
      console.log('点击注册按钮');
      
      // 基本验证
      if (this.enableMobileRegister) {
        if (!this.form.mobile) {
          uni.showToast({
            title: '请输入手机号',
            icon: 'none'
          });
          return;
        }
        if (!this.validateMobileInput(this.form.mobile)) {
          uni.showToast({
            title: '请输入正确的手机号',
            icon: 'none'
          });
          return;
        }
      } else {
        if (!this.form.username) {
          uni.showToast({
            title: '请输入用户名',
            icon: 'none'
          });
          return;
        }
      }
      
      if (!this.form.password) {
        uni.showToast({
          title: '请输入密码',
          icon: 'none'
        });
        return;
      }
      
      if (this.form.password !== this.form.confirmPassword) {
        uni.showToast({
          title: '两次输入密码不一致',
          icon: 'none'
        });
        return;
      }
      
      // 组装注册数据
      const registerData = {
        username: this.enableMobileRegister ? this.form.areaCode + this.form.mobile : this.form.username,
        password: this.form.password,
        confirmPassword: this.form.confirmPassword,
        captchaId: this.form.captchaId || this.generateUUID()
      };
      
      // 如果是手机号注册，添加手机验证码
      if (this.enableMobileRegister && this.form.mobileCaptcha) {
        registerData.mobileCaptcha = this.form.mobileCaptcha;
      }
      
      console.log('准备发送注册请求', registerData);
      
      // 调用注册接口
      uni.showLoading({
        title: '注册中...'
      });
      
      userApi.register(registerData)
        .then(res => {
          console.log('注册成功', res);
          uni.hideLoading();
          uni.showToast({
            title: '注册成功',
            icon: 'success'
          });
          
          // 注册成功后返回登录页或跳转到登录页
          setTimeout(() => {
            if (this.fromLogin) {
              // 如果是从登录页来的，直接返回
              uni.navigateBack();
            } else {
              // 否则跳转到登录页
              this.goToLogin();
            }
          }, 1500);
        })
        .catch(error => {
          console.error('注册失败', error);
          uni.hideLoading();
          uni.showToast({
            title: error?.message || error || '注册失败',
            icon: 'none',
            duration: 2000
          });
        });
    },

    // 跳转到登录页面
    goToLogin() {
      uni.navigateTo({
        url: '/pages/login/index'
      });
    },

    // 生成UUID
    generateUUID() {
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
      });
    },

    // 切换密码显示
    togglePasswordVisibility() {
      // 更新状态
      this.showPassword = !this.showPassword;
      
      // 强制重建组件
      this.renderPasswordInput = false;
      this.renderConfirmInput = false;
      
      // 缓存密码值
      const password = this.form.password;
      const confirmPassword = this.form.confirmPassword;
      
      // 强制清空密码
      this.form.password = '';
      this.form.confirmPassword = '';
      
      // 延时恢复组件
      setTimeout(() => {
        this.renderPasswordInput = true;
        this.renderConfirmInput = true;
        
        // 延时恢复值
        setTimeout(() => {
          this.form.password = password;
          this.form.confirmPassword = confirmPassword;
        }, 50);
      }, 50);
    }
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }
}
</script>

<style lang="scss">
.register-page {
  min-height: 100vh;
  background-color: #f8f8f8;
  display: flex;
  flex-direction: column;
  padding: 40rpx;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 40rpx;
  margin-bottom: 40rpx;
  
  .login-logo {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 20rpx;
  }
  
  .login-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #5778ff;
  }
}

.register-content {
  flex: 1;
}

.form-container {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.login-form__title {
  display: flex;
  flex-direction: column;
  margin-bottom: 40rpx;
}

.login-form__welcome {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.login-form__subtitle {
  font-size: 24rpx;
  color: #999;
}

.code-input {
  display: flex;
  align-items: center;
  
  .u-input {
    flex: 1;
    margin-right: 20rpx;
  }
  
  .captcha-image {
    width: 200rpx;
    height: 80rpx;
  }
}

.form-actions {
  margin-top: 40rpx;
}

.login-link {
  text-align: center;
  margin-top: 30rpx;
  font-size: 28rpx;
  color: #666;
  
  .link-text {
    color: #5778ff;
  }
}

.login-form__agreement {
  display: flex;
  justify-content: center;
  font-size: 24rpx;
  color: #999;
  margin-top: 30rpx;
  
  .agreement-link {
    color: #5778ff;
  }
}

// 添加密码输入框相关样式
.password-input-container {
  position: relative;
  
  .eye-icon {
    position: absolute;
    right: 20rpx;
    top: 50%;
    transform: translateY(-50%);
    z-index: 10;
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
