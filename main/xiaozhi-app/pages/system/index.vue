<template>
  <view class="system-page">
    <view class="system-menu">
      <view class="system-menu__title">系统管理</view>
      <view class="system-menu__list">
        <view class="system-menu__item" @click="goToPage('/pages/system/model/llm')">
          <u-icon name="setting-fill" size="44" color="#2979ff"></u-icon>
          <text class="system-menu__item-text">模型配置</text>
        </view>
        <view class="system-menu__item" @click="goToPage('/pages/system/ota')">
          <u-icon name="upgrade" size="44" color="#19be6b"></u-icon>
          <text class="system-menu__item-text">OTA管理</text>
        </view>
        <view class="system-menu__item" @click="goToPage('/pages/system/dict')">
          <u-icon name="list" size="44" color="#ff9900"></u-icon>
          <text class="system-menu__item-text">参数字典</text>
        </view>
        <view class="system-menu__item" @click="goToPage('/pages/system/user')" v-if="isAdmin">
          <u-icon name="account" size="44" color="#fa3534"></u-icon>
          <text class="system-menu__item-text">用户管理</text>
        </view>
      </view>
    </view>
    
    <view class="system-status">
      <view class="system-status__title">系统状态</view>
      <view class="system-status__card">
        <view class="system-status__item">
          <text class="system-status__label">服务器状态</text>
          <text class="system-status__value">正常</text>
        </view>
        <view class="system-status__item">
          <text class="system-status__label">当前版本</text>
          <text class="system-status__value">v1.0.0</text>
        </view>
        <view class="system-status__item">
          <text class="system-status__label">设备总数</text>
          <text class="system-status__value">{{ deviceCount }}</text>
        </view>
        <view class="system-status__item">
          <text class="system-status__label">用户总数</text>
          <text class="system-status__value">{{ userCount }}</text>
        </view>
      </view>
    </view>
    
    <view class="system-notice">
      <view class="system-notice__title">系统公告</view>
      <view class="system-notice__content">
        <text>欢迎使用小智ESP32管理系统，如有问题请联系管理员。</text>
      </view>
    </view>
  </view>
</template>

<script>
import { useUserStore } from '@/store/user';

export default {
  data() {
    return {
      deviceCount: 0,
      userCount: 0,
      isAdmin: false
    };
  },
  onLoad() {
    // 获取用户信息
    const userStore = useUserStore();
    this.isAdmin = userStore.isAdmin;
    
    // 获取系统状态
    this.getSystemStatus();
  },
  methods: {
    // 跳转到页面
    goToPage(url) {
      uni.navigateTo({
        url
      });
    },
    // 获取系统状态
    getSystemStatus() {
      // 模拟数据，实际项目中应该从API获取
      this.deviceCount = 12;
      this.userCount = 5;
    }
  }
};
</script>

<style lang="scss">
.system-page {
  padding: 20rpx;
  
  .system-menu {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
    margin-bottom: 20rpx;
    
    &__title {
      font-size: 32rpx;
      font-weight: bold;
      margin-bottom: 20rpx;
    }
    
    &__list {
      display: flex;
      flex-wrap: wrap;
    }
    
    &__item {
      width: 33.33%;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20rpx 0;
      
      &-text {
        font-size: 28rpx;
        margin-top: 10rpx;
      }
    }
  }
  
  .system-status {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
    margin-bottom: 20rpx;
    
    &__title {
      font-size: 32rpx;
      font-weight: bold;
      margin-bottom: 20rpx;
    }
    
    &__card {
      background-color: #f8f8f8;
      border-radius: 8rpx;
      padding: 20rpx;
    }
    
    &__item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    &__label {
      font-size: 28rpx;
      color: #666;
    }
    
    &__value {
      font-size: 28rpx;
      color: #333;
    }
  }
  
  .system-notice {
    background-color: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
    
    &__title {
      font-size: 32rpx;
      font-weight: bold;
      margin-bottom: 20rpx;
    }
    
    &__content {
      font-size: 28rpx;
      color: #666;
      line-height: 1.5;
    }
  }
}
</style> 