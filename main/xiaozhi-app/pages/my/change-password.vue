<template>
  <view class="change-password-page">
    
    <view class="change-password-content">
      <view class="form-container">
        <u-form :model="form" ref="uForm" :errorType="errorType" labelPosition="top" :borderBottom="false">
          <u-form-item label="原密码：" prop="oldPassword" labelWidth="160rpx" :required="true">
            <u-input 
              v-model="form.oldPassword" 
              type="password" 
              placeholder="请输入原密码" 
              :password="!showOldPassword"
              :class="{'input-error': errors.oldPassword}"
            >
              <template slot="suffix">
                <u-icon name="eye" size="40rpx" color="#979db1" @click="showOldPassword = !showOldPassword"></u-icon>
              </template>
            </u-input>
            <view v-if="errors.oldPassword" class="error-message">{{errors.oldPassword}}</view>
          </u-form-item>
          
          <u-form-item label="新密码：" prop="newPassword" labelWidth="160rpx" :required="true">
            <u-input 
              v-model="form.newPassword" 
              type="password" 
              placeholder="请输入新密码" 
              :password="!showNewPassword"
              :class="{'input-error': errors.newPassword}"
            >
              <template slot="suffix">
                <u-icon name="eye" size="40rpx" color="#979db1" @click="showNewPassword = !showNewPassword"></u-icon>
              </template>
            </u-input>
            <view v-if="errors.newPassword" class="error-message">{{errors.newPassword}}</view>
          </u-form-item>
          
          <u-form-item label="确认密码：" prop="confirmPassword" labelWidth="160rpx" :required="true">
            <u-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请再次输入新密码" 
              :password="!showConfirmPassword"
              :class="{'input-error': errors.confirmPassword}"
            >
              <template slot="suffix">
                <u-icon name="eye" size="40rpx" color="#979db1" @click="showConfirmPassword = !showConfirmPassword"></u-icon>
              </template>
            </u-input>
            <view v-if="errors.confirmPassword" class="error-message">{{errors.confirmPassword}}</view>
          </u-form-item>
        </u-form>
        
        <view class="password-tips">
          <u-icon name="info-circle" size="30rpx" color="#979db1"></u-icon>
          <text class="tips-text">密码修改成功后需要重新登录</text>
        </view>
        
        <view class="form-actions">
          <u-button type="primary" text="提交" @click="submitForm"></u-button>
          <u-button type="info" text="返回" plain @click="goBack"></u-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import userApi from '@/api/module/user';
import { useUserStore } from '@/store/user';

export default {
  data() {
    return {
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      showOldPassword: false,
      showNewPassword: false,
      showConfirmPassword: false,
      errorType: 'none',
      isSubmitting: false,
      // 错误消息
      errors: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      // 输入框边框颜色
      inputBorder: {
        oldPassword: 'bottom',
        newPassword: 'bottom',
        confirmPassword: 'bottom'
      }
    }
  },
  methods: {
    // 清除所有错误信息
    clearErrors() {
      this.errors = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
      this.inputBorder = {
        oldPassword: 'bottom',
        newPassword: 'bottom',
        confirmPassword: 'bottom'
      };
    },
    
    // 设置字段错误
    setFieldError(field, message) {
      this.errors[field] = message;
      this.inputBorder[field] = 'surround';
    },
    
    // 提交表单
    submitForm() {
      // 清除之前的错误信息
      this.clearErrors();
      
      // 手动验证表单
      if (!this.validateForm()) {
        return;
      }
      
      if (this.isSubmitting) {
        return;
      }
      
      this.isSubmitting = true;
      // 显示加载中
      uni.showLoading({
        title: '提交中...'
      });
      
      // 构建请求数据
      const data = {
        password: this.form.oldPassword,
        newPassword: this.form.newPassword
      };
      
      // 调用API修改密码
      userApi.changePassword(data)
        .then(() => {
          uni.hideLoading();
          uni.showToast({
            title: '密码修改成功，请重新登录',
            icon: 'success',
            duration: 2000
          });
          
          // 延迟退出登录
          setTimeout(() => {
            // 获取用户存储
            const userStore = useUserStore();
            // 清除登录状态
            userStore.clearToken();
            // 跳转到登录页
            uni.reLaunch({
              url: '/pages/login/index'
            });
          }, 2000);
        })
        .catch(error => {
          uni.hideLoading();
          uni.showToast({
            title: error || '修改密码失败',
            icon: 'none'
          });
          this.isSubmitting = false;
        });
    },
    
    // 自定义表单验证
    validateForm() {
      // 验证是否为空
      if (!this.form.oldPassword) {
        this.setFieldError('oldPassword', '请输入原密码');
        return false;
      }
      
      if (!this.form.newPassword) {
        this.setFieldError('newPassword', '请输入新密码');
        return false;
      }
      
      // 验证密码长度
      if (this.form.newPassword.length < 6 || this.form.newPassword.length > 20) {
        this.setFieldError('newPassword', '密码长度应在6-20个字符之间');
        return false;
      }
      
      if (!this.form.confirmPassword) {
        this.setFieldError('confirmPassword', '请再次输入新密码');
        return false;
      }
      
      // 验证两次密码是否一致
      if (this.form.newPassword !== this.form.confirmPassword) {
        this.setFieldError('confirmPassword', '两次输入的密码不一致');
        return false;
      }
      
      // 验证新密码是否与原密码相同
      if (this.form.oldPassword === this.form.newPassword) {
        this.setFieldError('newPassword', '新密码不能与原密码相同');
        return false;
      }
      
      return true;
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack();
    }
  }
}
</script>

<style lang="scss">
.change-password-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.change-password-content {
  flex: 1;
  padding: 30rpx;
}

.form-container {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.password-tips {
  display: flex;
  align-items: center;
  margin: 20rpx 0 30rpx;
  
  .tips-text {
    font-size: 24rpx;
    color: #979db1;
    margin-left: 10rpx;
  }
}

.form-actions {
  margin-top: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.error-message {
  font-size: 24rpx;
  color: #ff5b5b;
  margin-top: 5rpx;
}

.input-error {
  border: 1px solid #ff5b5b !important;
  border-radius: 4rpx;
}
</style>
