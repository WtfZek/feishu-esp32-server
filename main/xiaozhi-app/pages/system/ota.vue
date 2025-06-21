<template>
  <view class="ota-page">
    
    <view class="ota-content">
      <view class="ota-card">
        <view class="ota-header">
          <view class="ota-title">当前版本</view>
          <view class="ota-version">v1.0.0</view>
        </view>
        
        <view class="ota-info">
          <view class="info-item">
            <text class="info-label">设备型号：</text>
            <text class="info-value">ESP32-S3</text>
          </view>
          <view class="info-item">
            <text class="info-label">固件发布日期：</text>
            <text class="info-value">2023-12-01</text>
          </view>
        </view>
      </view>
      
      <view class="ota-actions">
        <u-button type="primary" text="检查更新"></u-button>
        <u-button type="info" plain text="手动升级"></u-button>
      </view>
      
      <view class="update-log">
        <view class="log-title">更新日志</view>
        <view class="log-content">
          <view class="log-item">
            <view class="log-version">v1.0.0 (2023-12-01)</view>
            <view class="log-desc">初始版本发布</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      // 暂不添加逻辑
    }
  }
}
</script>

<style lang="scss">
.ota-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.ota-content {
  flex: 1;
  padding: 30rpx;
}

.ota-card {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.ota-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.ota-title {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

.ota-version {
  font-size: 32rpx;
  color: #5778ff;
  font-weight: bold;
}

.ota-info {
  margin-top: 20rpx;
}

.info-item {
  display: flex;
  margin-bottom: 10rpx;
}

.info-label {
  font-size: 28rpx;
  color: #666;
  width: 200rpx;
}

.info-value {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.ota-actions {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.update-log {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
}

.log-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.log-item {
  margin-bottom: 20rpx;
}

.log-version {
  font-size: 28rpx;
  color: #5778ff;
  margin-bottom: 10rpx;
}

.log-desc {
  font-size: 28rpx;
  color: #666;
}
</style>
