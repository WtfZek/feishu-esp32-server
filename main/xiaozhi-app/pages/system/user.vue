<template>
  <view class="user-page">
    
    <view class="user-content">
      <view class="user-header">
        <view class="search-box">
          <u-search placeholder="搜索用户" v-model="searchText" :showAction="false"></u-search>
        </view>
        
        <view class="add-button">
          <u-button type="primary" icon="plus" text="添加用户"></u-button>
        </view>
      </view>
      
      <view class="user-list">
        <view class="user-item" v-for="index in 3" :key="index">
          <view class="user-info">
            <image class="user-avatar" src="/static/images/default-avatar.png"></image>
            <view class="user-details">
              <view class="user-name">用户{{ index }}</view>
              <view class="user-role">管理员</view>
            </view>
          </view>
          <view class="user-actions">
            <u-button type="primary" size="mini" text="编辑"></u-button>
            <u-button type="error" size="mini" text="删除"></u-button>
          </view>
        </view>
      </view>
      
      <view class="pagination">
        <view class="pagination-buttons">
          <u-button size="mini" :disabled="currentPage <= 1" @click="prevPage">上一页</u-button>
          <text class="pagination-text">{{ currentPage }}/{{ totalPages }}</text>
          <u-button size="mini" :disabled="currentPage >= totalPages" @click="nextPage">下一页</u-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      searchText: '',
      currentPage: 1,
      totalPages: 5
      // 暂不添加逻辑
    }
  },
  methods: {
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
      }
    },
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
      }
    }
  }
}
</script>

<style lang="scss">
.user-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.user-content {
  flex: 1;
  padding: 30rpx;
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.search-box {
  flex: 1;
  margin-right: 20rpx;
}

.user-list {
  background-color: #fff;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f5f6fa;
  
  &:last-child {
    border-bottom: none;
  }
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  margin-right: 20rpx;
}

.user-name {
  font-size: 30rpx;
  color: #333;
  margin-bottom: 10rpx;
}

.user-role {
  font-size: 24rpx;
  color: #999;
}

.user-actions {
  display: flex;
  gap: 20rpx;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30rpx;
}

.pagination-buttons {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.pagination-text {
  font-size: 28rpx;
  color: #666;
}
</style>
