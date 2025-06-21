<template>
  <view class="login-page">
    <!-- 顶部图片 -->
    <view class="login-header">
      <image class="login-logo" src="/static/logo.png" mode="aspectFit"></image>
      <text class="login-title">飞鼠智控终端</text>
    </view>
    
    <!-- 登录表单 -->
    <view class="login-form">
      <view class="login-form__title">
        <text class="login-form__welcome">欢迎登录</text>
        <text class="login-form__subtitle">WELCOME TO LOGIN</text>
      </view>
      
      <!-- 用户名登录 -->
      <view v-if="!isMobileLogin">
        <view class="login-form__item">
          <u-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefixIcon="account"
            border="bottom"
            clearable
          ></u-input>
        </view>
      </view>
      
      <!-- 手机号登录 -->
      <view v-else>
        <view class="login-form__item">
          <u-input
            v-model="form.mobile"
            type="number"
            placeholder="请输入手机号码"
            prefixIcon="phone"
            border="bottom"
            clearable
          ></u-input>
        </view>
      </view>
      
      <!-- 密码 -->
      <view class="login-form__item">
        <u-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          prefixIcon="lock"
          border="bottom"
          clearable
          :password="!showPassword"
          suffixIcon="eye"
          @suffixIconClick="showPassword = !showPassword"
        ></u-input>
      </view>
      
      <!-- 验证码 -->
      <!-- <view class="login-form__captcha">
        <u-input
          v-model="form.captcha"
          placeholder="请输入验证码"
          prefixIcon="checkmark"
          border="bottom"
          clearable
          style="flex: 1;"
        ></u-input>
        <image 
          class="captcha-image" 
          :src="captchaUrl" 
          @click="fetchCaptcha" 
          v-if="captchaUrl"
        ></image>
      </view> -->
      
      <!-- 登录方式切换 -->
      <view class="login-form__switch" v-if="enableMobileLogin">
        <text @click="switchLoginType" class="switch-text">
          {{ isMobileLogin ? '使用用户名登录' : '使用手机号登录' }}
        </text>
      </view>
      
      <!-- 忘记密码和注册 -->
      <view class="login-form__links">
        <text @click="goToRegister" class="link-text" v-if="allowUserRegister">新用户注册</text>
        <text @click="goToForgetPassword" class="link-text" v-if="enableMobileLogin">忘记密码?</text>
      </view>
      
      <!-- 登录按钮 -->
      <view class="login-form__button">
        <u-button type="primary" text="登录" @click="login"></u-button>
      </view>
      
      <!-- 用户协议 -->
      <view class="login-form__agreement">
        <text class="agreement-text">登录即同意</text>
        <text class="agreement-link">《用户协议》</text>
        <text class="agreement-text">和</text>
        <text class="agreement-link">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import userApi from '@/api/module/user';
import { validateMobile } from '@/utils/validate';

// 表单数据
const form = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaId: '',
  mobile: ''
});

// 状态变量
const captchaUrl = ref('');
const captchaUuid = ref('');
const showPassword = ref(false);
const isMobileLogin = ref(false);
const enableMobileLogin = ref(false);
const allowUserRegister = ref(false);

// 用户存储
const userStore = useUserStore();

// 生命周期钩子
onMounted(() => {
  fetchCaptcha();
  getPubConfig();
});

// 获取验证码
const fetchCaptcha = async () => {
  captchaUuid.value = generateUUID();
  try {
    // 获取验证码URL
    captchaUrl.value = await userApi.getCaptcha(captchaUuid.value);
    console.log('获取到验证码URL:', captchaUrl.value);
  } catch (error) {
    console.error('获取验证码失败', error);
    uni.showToast({
      title: '验证码加载失败，点击刷新',
      icon: 'none'
    });
  }
};

// 获取公共配置
const getPubConfig = async () => {
  try {
    const config = await userApi.getPubConfig();
    enableMobileLogin.value = config.enableMobileLogin || false;
    allowUserRegister.value = config.allowUserRegister || false;
  } catch (error) {
    console.error('获取公共配置失败', error);
  }
};

// 切换登录方式
const switchLoginType = () => {
  isMobileLogin.value = !isMobileLogin.value;
  form.username = '';
  form.mobile = '';
  form.password = '';
  form.captcha = '';
  fetchCaptcha();
};

// 登录
const login = async () => {
  // 表单验证
  if (isMobileLogin.value) {
    // 手机号登录验证
    if (!form.mobile) {
      uni.showToast({
        title: '请输入手机号码',
        icon: 'none'
      });
      return;
    }
    if (!validateMobile(form.mobile)) {
      uni.showToast({
        title: '请输入正确的手机号码',
        icon: 'none'
      });
      return;
    }
    // 使用手机号作为用户名
    form.username = form.mobile;
  } else {
    // 用户名登录验证
    if (!form.username) {
      uni.showToast({
        title: '请输入用户名',
        icon: 'none'
      });
      return;
    }
  }
  
  // 密码验证
  if (!form.password) {
    uni.showToast({
      title: '请输入密码',
      icon: 'none'
    });
    return;
  }
  
  // 验证码验证
  // if (!form.captcha) {
  //   uni.showToast({
  //     title: '请输入验证码',
  //     icon: 'none'
  //   });
  //   return;
  // }
  
  // 添加验证码ID
  form.captchaId = captchaUuid.value;
  
  try {
    // 登录请求
    await userStore.login(form);
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    });
    
    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/agent/index'
      });
    }, 1500);
  } catch (error) {
    console.error('登录失败', error);
    uni.showToast({
      title: error || '登录失败',
      icon: 'none'
    });
    
    // 刷新验证码
    fetchCaptcha();
  }
};

// 跳转到注册页面
const goToRegister = () => {
  uni.navigateTo({
    url: '/pages/login/register?from=login'
  });
};

// 跳转到忘记密码页面
const goToForgetPassword = () => {
  uni.navigateTo({
    url: '/pages/my/forget-password'
  });
};

// 生成UUID
const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
};
</script>

<style lang="scss">
.login-page {
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
  margin-top: 60rpx;
  margin-bottom: 60rpx;
  
  .login-logo {
    width: 240rpx;
    height: 240rpx;
    margin-bottom: 20rpx;
  }
  
  .login-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #5778ff;
  }
}

.login-form {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  &__title {
    display: flex;
    flex-direction: column;
    margin-bottom: 60rpx;
  }
  
  &__welcome {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 10rpx;
  }
  
  &__subtitle {
    font-size: 24rpx;
    color: #999;
  }
  
  &__item {
    margin-bottom: 30rpx;
  }
  
  &__captcha {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
    
    .captcha-image {
      width: 200rpx;
      height: 80rpx;
      margin-left: 20rpx;
    }
  }
  
  &__switch {
    text-align: right;
    margin-bottom: 20rpx;
    
    .switch-text {
      font-size: 24rpx;
      color: #5778ff;
    }
  }
  
  &__links {
    display: flex;
    justify-content: space-between;
    margin-bottom: 40rpx;
    
    .link-text {
      font-size: 24rpx;
      color: #5778ff;
    }
  }
  
  &__button {
    margin-bottom: 30rpx;
  }
  
  &__agreement {
    display: flex;
    justify-content: center;
    font-size: 24rpx;
    color: #999;
    
    .agreement-link {
      color: #5778ff;
    }
  }
}
</style>
