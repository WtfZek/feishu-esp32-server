<template>
  <view class="creation-page">
    <!-- 页面头部 -->
    <view class="creation-header">
      <text class="creation-title">创作社区</text>
      <text class="creation-subtitle">共创和谐社区，共享优质智能助手</text>
    </view>
    
    <scroll-view 
      class="creation-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view class="creation-empty" v-if="agentList.length === 0">
        <u-empty mode="list" icon="">
          <text slot="message">暂无已发布的智能助手</text>
        </u-empty>
      </view>
      <view class="creation-items" v-else>
        <view class="creation-item" v-for="(item, index) in agentList" :key="index">
          <view class="creation-item__header" @click="viewAgentDetail(item)">
            <view class="creation-item__avatar-name">
              <image class="creation-item__avatar" :src="item.agentAvatar || '/static/avatar/default_toy_avatar.jpeg'" mode="aspectFit"></image>
              <text class="creation-item__name">{{ item.agentName }}</text>
            </view>
            <view class="creation-item__clone-btn" @click.stop="cloneAgent(item)">
              <u-icon name="plus" color="#fff" size="32rpx"></u-icon>
            </view>
          </view>
          <view class="creation-item__content" @click="viewAgentDetail(item)">
            <view class="creation-item__info">
              <view class="creation-item__info-item">
                <text class="creation-item__info-label">音色模型：</text>
                <text class="creation-item__info-value">{{ item.ttsVoiceName }}</text>
              </view>
              <view class="creation-item__prompt-box" v-if="item.systemPrompt">
                <scroll-view scroll-y class="creation-item__prompt-scroll">
                  <text class="creation-item__prompt-text">{{ item.systemPrompt }}</text>
                </scroll-view>
              </view>
            </view>
          </view>
          <view class="creation-item__footer">
            <view class="creation-item__stats">
              <view class="creation-item__stat-item" @click.stop="toggleLike(item)">
                <u-icon :name="item.isLiked ? 'heart-fill' : 'heart'" size="40rpx" :color="item.isLiked ? '#FF5B8F' : '#979db1'"></u-icon>
                <text class="creation-item__stat-text">{{ item.likes || 0 }}</text>
              </view>
              <view class="creation-item__stat-item" @click.stop="shareAgent(item)">
                <u-icon name="share" size="40rpx" color="#979db1"></u-icon>
                <text class="creation-item__stat-text">分享</text>
              </view>
              <view class="creation-item__stat-item">
                <u-icon name="download" size="40rpx" color="#979db1"></u-icon>
                <text class="creation-item__stat-text">{{ item.deviceCount || 0 }}</text>
              </view>
            </view>
            <text class="creation-item__time" v-if="item.lastConnectedAt">最近更新: {{ $filters.formatDate(item.lastConnectedAt, 'YYYY-MM-DD') }}</text>
          </view>
        </view>
        
        <!-- 加载更多提示 -->
        <view class="loading-more" v-if="agentList.length > 0">
          <text v-if="isLoading">加载中...</text>
          <text v-else-if="hasMore">上拉加载更多</text>
          <text v-else>没有更多数据了</text>
        </view>
        
        <view style="height: 30rpx;"></view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import agentApi from '@/api/module/agent';

export default {
  data() {
    return {
      agentList: [],
      refreshing: false,
      likedAgents: {}, // 存储用户点赞状态
      
      // 分页相关
      currentPage: 1,
      pageSize: 5,
      total: 0,
      hasMore: true,
      isLoading: false
    };
  },
  onLoad() {
    // 从本地存储加载点赞状态
    try {
      const likedAgentsStr = uni.getStorageSync('likedAgents');
      if (likedAgentsStr) {
        this.likedAgents = JSON.parse(likedAgentsStr);
      }
    } catch (e) {
      console.error('获取点赞状态失败', e);
    }
    
    this.getAgentList();
  },
  onShow() {
    // 每次页面显示时刷新数据
    this.resetAndRefresh();
  },
  onPullDownRefresh() {
    this.resetAndRefresh();
    uni.stopPullDownRefresh();
  },
  methods: {
    // 重置数据并刷新
    resetAndRefresh() {
      this.currentPage = 1;
      this.agentList = [];
      this.hasMore = true;
      this.getAgentList();
    },
    
    // 获取已发布的智能体列表
    async getAgentList() {
      if (!this.hasMore || this.isLoading) return;
      
      try {
        this.isLoading = true;
        
        if (this.currentPage === 1) {
          uni.showLoading({
            title: '加载中...',
            mask: true
          });
        }
        
        // 构建分页参数
        const params = {
          page: this.currentPage,
          limit: this.pageSize
        };
        
        // 构建筛选条件（如果有）
        // 如果有筛选需求，可以在这里添加筛选条件
        // const filter = { ttsVoiceName: '湾湾小何' };
        const filter = null;
        
        // 调用API获取数据
        const res = await agentApi.getPublishedAgentList(params, filter);
        
        // 处理后端返回的分页数据
        const { list, total } = res;
        
        // 处理列表数据，添加点赞状态
        const processedList = list.map(item => {
          return {
            ...item,
            isLiked: !!this.likedAgents[item.id],
            likes: Math.floor(Math.random() * 1000) // 模拟点赞数据，实际应从后端获取
          };
        });
        
        // 追加或替换数据
        if (this.currentPage === 1) {
          this.agentList = processedList;
        } else {
          this.agentList = [...this.agentList, ...processedList];
        }
        
        // 更新分页信息
        this.total = total;
        this.hasMore = this.agentList.length < total;
        this.currentPage++;
      } catch (error) {
        console.error('获取智能体列表失败', error);
        uni.showToast({
          title: '获取列表失败',
          icon: 'none'
        });
      } finally {
        this.isLoading = false;
        uni.hideLoading();
        
        // 如果是下拉刷新触发的，结束刷新状态
        if (this.refreshing) {
          this.refreshing = false;
        }
      }
    },
    
    // 加载更多
    loadMore() {
      if (this.hasMore && !this.isLoading) {
        this.getAgentList();
      }
    },
    
    // 查看智能体详情
    viewAgentDetail(item) {
      uni.navigateTo({
        url: `/pages/creation/pub-agent-info?id=${item.id}`
      });
    },
    
    // 克隆智能体
    async cloneAgent(item) {
      try {
        uni.showLoading({
          title: '克隆中...',
          mask: true
        });
        
        const result = await agentApi.cloneAgent(item.id);
        
        uni.hideLoading();
        uni.showToast({
          title: '克隆成功',
          icon: 'success'
        });
        
        // 克隆成功后跳转到我的智能体页面
        setTimeout(() => {
          uni.switchTab({
            url: '/pages/agent/index'
          });
        }, 500);
      } catch (error) {
        uni.hideLoading();
        console.error('克隆智能体失败', error);
        uni.showToast({
          title: '克隆失败: ' + (error.msg || '未知错误'),
          icon: 'none',
          duration: 3000
        });
      }
    },
    
    // 切换点赞状态
    toggleLike(item) {
      // 切换点赞状态
      item.isLiked = !item.isLiked;
      
      // 更新点赞数
      if (item.isLiked) {
        item.likes++;
        this.likedAgents[item.id] = true;
      } else {
        item.likes--;
        delete this.likedAgents[item.id];
      }
      
      // 保存点赞状态到本地存储
      try {
        uni.setStorageSync('likedAgents', JSON.stringify(this.likedAgents));
      } catch (e) {
        console.error('保存点赞状态失败', e);
      }
      
      // 显示点赞反馈
      uni.showToast({
        title: item.isLiked ? '已点赞' : '已取消点赞',
        icon: 'none'
      });
    },
    
    // 分享智能体
    shareAgent(item) {
      uni.showShareMenu({
        withShareTicket: true,
        menus: ['shareAppMessage', 'shareTimeline']
      });
      
      // 显示分享提示
      uni.showToast({
        title: '分享功能开发中',
        icon: 'none'
      });
    },
    
    // 下拉刷新
    onRefresh() {
      this.refreshing = true;
      this.resetAndRefresh();
    }
  }
};
</script>

<style lang="scss">
.creation-page {
  height: 100vh;
  background-color: #f8f8f8;
  position: relative;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.creation-header {
  padding: 30rpx;
  background: linear-gradient(135deg, #5778ff, #6b8aff);
  color: #fff;
  
  .creation-title {
    font-size: 44rpx;
    font-weight: bold;
    margin-bottom: 10rpx;
    display: block;
  }
  
  .creation-subtitle {
    font-size: 28rpx;
    opacity: 0.9;
  }
}

.creation-list {
  flex: 1;
  padding: 20rpx 20rpx 0 20rpx;
  height: 0;
  
  .creation-empty {
    padding: 100rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
  
  .loading-more {
    text-align: center;
    padding: 20rpx 0;
    
    text {
      font-size: 26rpx;
      color: #8a8a8a;
    }
  }
  
  .creation-items {
    .creation-item {
      background-color: #fff;
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 30rpx;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
      position: relative;
      
      &__header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;
      }
      
      &__avatar-name {
        display: flex;
        align-items: center;
        gap: 20rpx;
      }
      
      &__avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        background-color: #f0f0f0;
        border: 2rpx solid #eaeaea;
      }
      
      &__name {
        font-size: 36rpx;
        font-weight: bold;
        color: #3d4566;
      }
      
      &__clone-btn {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
        background-color: #4cd964;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2rpx 8rpx rgba(76, 217, 100, 0.3);
        margin-right: 20rpx;
      }
      
      &__content {
        margin-bottom: 20rpx;
      }
      
      &__info {
        &-item {
          display: flex;
          margin-bottom: 16rpx;
        }
        
        &-label {
          font-size: 28rpx;
          color: #666;
          // width: 120rpx;
        }
        
        &-value {
          font-size: 28rpx;
          color: #333;
          font-weight: bold;
        }
      }
      
      &__prompt-box {
        margin-top: 20rpx;
        margin-bottom: 20rpx;
        border-radius: 12rpx;
        background-color: #f8f8f8;
        padding: 16rpx;
      }
      
      &__prompt-scroll {
        max-height: 160rpx;
      }
      
      &__prompt-text {
        font-size: 26rpx;
        color: #666;
        line-height: 1.5;
        word-break: break-all;
        white-space: pre-wrap;
      }
      
      &__footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      &__stats {
        display: flex;
        gap: 30rpx;
      }
      
      &__stat-item {
        display: flex;
        align-items: center;
        gap: 8rpx;
      }
      
      &__stat-text {
        font-size: 24rpx;
        color: #979db1;
      }
      
      &__time {
        font-size: 24rpx;
        color: #979db1;
      }
    }
  }
}
</style> 