<template>
  <view class="dict-page">
    
    <view class="dict-content">
      <view class="placeholder-text">数据字典功能开发中...</view>
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
.dict-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.dict-content {
  flex: 1;
  padding: 30rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.placeholder-text {
  font-size: 32rpx;
  color: #999;
}
</style>
