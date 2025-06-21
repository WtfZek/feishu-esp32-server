<template>
  <view class="profile-page">
    
    <view class="profile-content">
      <view class="form-container">
        <u-form :model="form" ref="uForm">
          <u-form-item label="头像" prop="avatar">
            <view class="avatar-wrapper">
              <image class="avatar-image" src="/static/images/default-avatar.png" mode="aspectFill"></image>
              <view class="avatar-upload">点击上传</view>
            </view>
          </u-form-item>
          
          <u-form-item label="用户名" prop="username">
            <u-input v-model="form.username" placeholder="请输入用户名"></u-input>
          </u-form-item>
          
          <u-form-item label="昵称" prop="nickname">
            <u-input v-model="form.nickname" placeholder="请输入昵称"></u-input>
          </u-form-item>
          
          <u-form-item label="手机号" prop="phone">
            <u-input v-model="form.phone" placeholder="请输入手机号"></u-input>
          </u-form-item>
          
          <u-form-item label="邮箱" prop="email">
            <u-input v-model="form.email" placeholder="请输入邮箱"></u-input>
          </u-form-item>
        </u-form>
        
        <view class="form-actions">
          <u-button type="primary" text="保存"></u-button>
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
        avatar: '',
        username: '',
        nickname: '',
        phone: '',
        email: ''
      }
      // 暂不添加逻辑
    }
  }
}
</script>

<style lang="scss">
.profile-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.profile-content {
  flex: 1;
  padding: 30rpx;
}

.form-container {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
}

.avatar-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
}

.avatar-upload {
  margin-left: 20rpx;
  color: #5778ff;
  font-size: 28rpx;
}

.form-actions {
  margin-top: 40rpx;
}
</style>
