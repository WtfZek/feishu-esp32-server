<template>
  <view class="my-page">
    <view class="my-header">
      <view class="my-header__avatar">
        <u-avatar size="100" :src="userInfo.avatar || '/static/avatar/default_user_avatar.jpeg'"></u-avatar>
      </view>
      <view class="my-header__info">
        <view class="my-header__name">{{ userInfo.username || '未登录' }}</view>
        <view class="my-header__mobile" :class="{'clickable': !isLoggedIn}" @click="handleMobileClick">
          {{ isLoggedIn ? '普通用户' : '点击登录' }}
        </view>
      </view>
    </view>
    
    <view class="my-menu">
      <u-cell-group>
        <u-cell title="个人资料" disabled icon="account" isLink @click="goToPage('/pages/my/profile')"></u-cell>
        <u-cell title="修改密码" icon="lock" isLink @click="goToPage('/pages/my/change-password')"></u-cell>
        <u-cell title="设置" icon="setting" isLink @click="goToPage('/pages/my/setting')"></u-cell>
      </u-cell-group>
    </view>
    
    <view class="my-menu">
      <u-cell-group>
        <u-cell title="关于我们" icon="info-circle" isLink @click="goToPage('/pages/my/about')"></u-cell>
        <u-cell title="意见反馈" icon="edit-pen" isLink @click="goToPage('/pages/my/feedback')"></u-cell>
      </u-cell-group>
    </view>
    
    <view class="my-logout" v-if="isLoggedIn">
      <u-button type="error" text="退出登录" @click="logout"></u-button>
    </view>
    
    <view class="my-version">
      <!-- 引用package.json中的version -->
      <text>当前版本：{{ version }}</text>
    </view>
  </view>
</template>

<script>
import { useUserStore } from '@/store/user';
import appConfig from '@/utils/config.js';

export default {
  data() {
    return {
      userInfo: {},
      isLoggedIn: false,
      version: appConfig.version
    };
  },
  onShow() {
    // 获取用户信息
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
    // 跳转到页面
    goToPage(url) {
      // 如果未登录且不是登录页，则跳转到登录页
      if (!this.isLoggedIn && url !== '/pages/login/index') {
        uni.navigateTo({
          url: '/pages/login/index'
        });
        return;
      }
      
      uni.navigateTo({
        url
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
    handleMobileClick() {
      if (!this.isLoggedIn) {
        this.goToPage('/pages/login/index');
      }
    }
  }
};
</script>

<style lang="scss">
.my-page {
  min-height: 100vh;
  background-color: #f8f8f8;
  
  .my-header {
    height: 160px;
    background: linear-gradient(to right, #2979ff, #5cadff);
    display: flex;
    align-items: center;
    padding: 0 30rpx;
    
    &__avatar {
      margin-right: 30rpx;
    }
    
    &__info {
      color: #fff;
    }
    
    &__name {
      font-size: 36rpx;
      font-weight: bold;
      margin-bottom: 10rpx;
    }
    
    &__mobile {
      font-size: 28rpx;
      opacity: 0.8;
      cursor: pointer;
    }
    
    &__mobile.clickable {
      text-decoration: underline;
    }
  }
  
  .my-menu {
    margin: 20rpx 0;
  }
  
  .my-logout {
    margin: 40rpx 30rpx;
  }
  
  .my-version {
    text-align: center;
    font-size: 24rpx;
    color: #999;
    margin: 40rpx 0;
  }
}
</style> 