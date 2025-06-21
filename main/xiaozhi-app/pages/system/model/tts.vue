<template>
  <view class="tts-model-page">
    
    <view class="model-content">
      <view class="model-header">
        <view class="search-box">
          <u-search placeholder="搜索TTS模型" v-model="searchText" :showAction="false"></u-search>
        </view>
        
        <view class="add-button">
          <u-button type="primary" icon="plus" text="添加模型"></u-button>
        </view>
      </view>
      
      <view class="model-list">
        <view class="model-item" v-for="index in 3" :key="index">
          <view class="model-info">
            <view class="model-icon">
              <u-icon name="mic-fill" size="50" color="#FF5B8F"></u-icon>
            </view>
            <view class="model-details">
              <view class="model-name">TTS模型{{ index }}</view>
              <view class="model-desc">支持多音色实时语音合成</view>
            </view>
          </view>
          <view class="model-status">
            <u-tag text="已启用" type="success" v-if="index === 1"></u-tag>
            <u-tag text="未启用" type="info" v-else></u-tag>
          </view>
          <view class="model-actions">
            <u-button type="primary" size="mini" text="配置"></u-button>
            <u-button type="warning" size="mini" text="音色"></u-button>
            <u-button type="error" size="mini" text="删除"></u-button>
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
      searchText: ''
      // 暂不添加逻辑
    }
  }
}
</script>

<style lang="scss">
.tts-model-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.model-content {
  flex: 1;
  padding: 30rpx;
}

.model-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.search-box {
  flex: 1;
  margin-right: 20rpx;
}

.model-list {
  background-color: #fff;
  border-radius: 12rpx;
}

.model-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f5f6fa;
  
  &:last-child {
    border-bottom: none;
  }
}

.model-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.model-icon {
  margin-right: 20rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.model-name {
  font-size: 30rpx;
  color: #333;
  margin-bottom: 10rpx;
}

.model-desc {
  font-size: 24rpx;
  color: #999;
}

.model-status {
  margin-right: 20rpx;
}

.model-actions {
  display: flex;
  gap: 20rpx;
}
</style>
