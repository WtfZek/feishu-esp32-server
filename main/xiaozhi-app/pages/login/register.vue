<template>
  <view class="register-page">
    
    <view class="register-content">
      <view class="form-container">
        <u-form :model="form" ref="uForm">
          <u-form-item label="用户名" prop="username">
            <u-input v-model="form.username" placeholder="请输入用户名"></u-input>
          </u-form-item>
          
          <u-form-item label="密码" prop="password">
            <u-input v-model="form.password" type="password" placeholder="请输入密码"></u-input>
          </u-form-item>
          
          <u-form-item label="确认密码" prop="confirmPassword">
            <u-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"></u-input>
          </u-form-item>
          
          <u-form-item label="手机号" prop="phone">
            <u-input v-model="form.phone" placeholder="请输入手机号"></u-input>
          </u-form-item>
          
          <u-form-item label="验证码" prop="code">
            <view class="code-input">
              <u-input v-model="form.code" placeholder="请输入验证码"></u-input>
              <u-button type="primary" size="mini" text="获取验证码"></u-button>
            </view>
          </u-form-item>
        </u-form>
        
        <view class="form-actions">
          <u-button type="primary" text="注册"></u-button>
        </view>
        
        <view class="login-link">
          已有账号？<text class="link-text">去登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        phone: '',
        code: ''
      }
      // 暂不添加逻辑
    }
  }
}
</script>

<style lang="scss">
.register-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.register-content {
  flex: 1;
  padding: 30rpx;
}

.form-container {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
}

.code-input {
  display: flex;
  align-items: center;
  
  .u-input {
    flex: 1;
    margin-right: 20rpx;
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
</style>
