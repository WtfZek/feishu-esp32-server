<template>
  <view class="about-page">
    <view class="about-header">
      <image class="about-logo" src="/static/logo.png" mode="aspectFit"></image>
      <text class="about-title">飞鼠智控终端</text>
      <text class="about-version">v1.0.0</text>
    </view>
    
    <view class="about-content">
      <view class="about-section">
        <view class="section-title">公司信息</view>
        <view class="info-item">
          <text class="info-label">公司名称</text>
          <text class="info-value">武汉飞鼠科技有限公司</text>
        </view>
        <view class="info-item">
          <text class="info-label">公司地址</text>
          <text class="info-value">武汉市洪山区光谷大道光谷科技园</text>
        </view>
      </view>
      
      <view class="about-section">
        <view class="section-title">联系方式</view>
        <view class="info-item">
          <text class="info-label">官方网站</text>
          <text class="info-value">www.ai-tulip.site</text>
        </view>
        <view class="info-item">
          <text class="info-label">联系电话</text>
          <text class="info-value">027-88889999</text>
        </view>
        <view class="info-item">
          <text class="info-label">电子邮箱</text>
          <text class="info-value">contact@feishu-ai.site</text>
        </view>
      </view>
      
      <view class="about-section">
        <view class="section-title">备案信息</view>
        <view class="info-item">
          <text class="info-value">鄂ICP备20230001号-1</text>
        </view>
      </view>
    </view>
    
    <view class="about-footer">
      <text class="copyright">© 2023-2025 武汉飞鼠科技有限公司 版权所有</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  }
}
</script>

<style lang="scss">
.about-page {
  min-height: 94vh;
  background-color: #f5f6fa;
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
}

.about-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0;
  
  .about-logo {
    width: 160rpx;
    height: 160rpx;
    border-radius: 20rpx;
    margin-bottom: 20rpx;
  }
  
  .about-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 10rpx;
  }
  
  .about-version {
    font-size: 24rpx;
    color: #999;
  }
}

.about-content {
  flex: 1;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin: 30rpx 0;
}

.about-section {
  margin-bottom: 40rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
    position: relative;
    padding-left: 20rpx;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 6rpx;
      height: 24rpx;
      background-color: #1890ff;
      border-radius: 3rpx;
    }
  }
}

.info-item {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .info-label {
    width: 160rpx;
    font-size: 28rpx;
    color: #666;
  }
  
  .info-value {
    flex: 1;
    font-size: 28rpx;
    color: #333;
  }
}

.about-footer {
  text-align: center;
  padding: 30rpx 0;
  
  .copyright {
    font-size: 24rpx;
    color: #999;
  }
}
</style>




