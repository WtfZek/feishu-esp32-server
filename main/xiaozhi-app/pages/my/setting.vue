<template>
  <view class="setting-page">
    <view class="setting-content">
      <view class="setting-group">
        <view class="setting-title">账号设置</view>
        <view class="setting-list">
          <view class="setting-item" isLink @click="goToPage('/pages/my/change-password')">
            <text class="setting-item-text">修改密码</text>
            <u-icon name="arrow-right" size="28rpx" color="#c0c4cc"></u-icon>
          </view>
          
          <view class="setting-item" @click="showDevelopingTip">
            <text class="setting-item-text">隐私设置</text>
            <u-icon name="arrow-right" size="28rpx" color="#c0c4cc"></u-icon>
          </view>
        </view>
      </view>
      
      <view class="setting-group">
        <view class="setting-title">通用设置</view>
        <view class="setting-list">
          <view class="setting-item">
            <text class="setting-item-text">通知设置</text>
            <u-switch v-model="notificationEnabled" @change="handleNotificationChange"></u-switch>
          </view>
          
          <view class="setting-item">
            <text class="setting-item-text">深色模式</text>
            <u-switch v-model="darkModeEnabled" @change="handleDarkModeChange"></u-switch>
          </view>
          
          <view class="setting-item" @click="showDevelopingTip">
            <text class="setting-item-text">语言</text>
            <view class="setting-value">
              <text>简体中文</text>
              <u-icon name="arrow-right" size="28rpx" color="#c0c4cc"></u-icon>
            </view>
          </view>
        </view>
      </view>
      
      <view class="setting-group">
        <view class="setting-title">关于</view>
        <view class="setting-list">
          <view class="setting-item" isLink @click="goToPage('/pages/my/about')">
            <text class="setting-item-text">关于我们</text>
            <u-icon name="arrow-right" size="28rpx" color="#c0c4cc"></u-icon>
          </view>
          
          <view class="setting-item">
            <text class="setting-item-text">版本</text>
            <view class="setting-value">
              <text>{{ version }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <view class="logout-button">
        <u-button type="error" text="退出登录" @click="logout"></u-button>
      </view>
    </view>
  </view>
</template>

<script>
import { useUserStore } from '@/store/user';
import appConfig from '@/utils/config.js';

export default {
  data() {
    return {
      isLoggedIn: false,
      userInfo: {},
      version: appConfig.version,
      notificationEnabled: true,
      darkModeEnabled: false
    }
  },
  onShow() {
    this.getUserInfo();
  },
  methods: {
    // 获取用户信息
    getUserInfo() {
      const userStore = useUserStore();
      this.isLoggedIn = userStore.isLoggedIn;
      if (this.isLoggedIn) {
        this.userInfo = userStore.userInfo || {};
      } else {
        this.userInfo = {};
      }
    },
    goToPage(url) {
      if (!this.isLoggedIn && url !== '/pages/login/index') {
        uni.navigateTo({
          url: '/pages/login/index'
        });
        return;
      }
      uni.navigateTo({ url });
    },
    // 未实现功能的统一提示
    showDevelopingTip() {
      uni.showToast({
        title: '功能正在开发中...',
        icon: 'none'
      });
    },
    // 通知设置切换提示
    handleNotificationChange() {
      uni.showToast({
        title: '功能正在开发中...',
        icon: 'none'
      });
    },
    // 深色模式切换提示
    handleDarkModeChange() {
      uni.showToast({
        title: '功能正在开发中...',
        icon: 'none'
      });
    },
    // 退出登录
    logout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            const userStore = useUserStore();
            userStore.logout();
          }
        }
      });
    },
  }
}
</script>

<style lang="scss">
.setting-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.setting-content {
  flex: 1;
  padding: 30rpx;
}

.setting-group {
  margin-bottom: 30rpx;
}

.setting-title {
  font-size: 28rpx;
  color: #909399;
  margin-bottom: 10rpx;
  padding-left: 10rpx;
}

.setting-list {
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f5f6fa;
  
  &:last-child {
    border-bottom: none;
  }
}

.setting-item-text {
  font-size: 30rpx;
  color: #303133;
}

.setting-value {
  display: flex;
  align-items: center;
  font-size: 30rpx;
  color: #909399;
  
  text {
    margin-right: 10rpx;
  }
}

.logout-button {
  margin-top: 50rpx;
}
</style>
