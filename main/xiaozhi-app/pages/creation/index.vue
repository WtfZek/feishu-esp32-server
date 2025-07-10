<template>
  <view class="creation-page">
    <!-- 页面头部 -->
    <view class="creation-header">
      <image class="creation-header__bg" src="https://jh-vioce.oss-cn-shanghai.aliyuncs.com/image/9aabe995_ban2.png" mode="aspectFill"></image>
      <view class="creation-header__content">
        <text class="creation-title">创作社区</text>
        <text class="creation-subtitle">共创和谐社区，共享优质智能助手</text>
      </view>
    </view>
    
    <view class="creation-content-body">
      <!-- 搜索区域 -->
      <view class="creation-search">
        <view class="creation-search__wrapper">
          <view class="creation-search__dropdown" @click="showVoiceSelector = true">
            <text class="creation-search__dropdown-text">{{ selectedVoice || '音色' }}</text>
            <u-icon name="arrow-down-fill" size="24rpx" color="#666"></u-icon>
          </view>
          <view class="creation-search__divider"></view>
          <input
            class="creation-search__input"
            v-model="searchKeyword"
            placeholder="根据音色搜索智能助手"
            confirm-type="search"
            @confirm="handleSearch"
          />
          <view class="creation-search__clear" v-if="searchKeyword || selectedVoice" @click="clearSearch">
            <u-icon name="close-circle-fill" size="36rpx" color="#999"></u-icon>
          </view>
          <view class="creation-search__icon" @click="handleSearch">
            <u-icon name="search" size="40rpx" color="#5778ff"></u-icon>
          </view>
        </view>
      </view>

      <scroll-view
        class="creation-list-scroll-view"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="loadMore"
      >
        <view class="creation-empty" v-if="agentList.length === 0">
          <u-empty mode="list" icon="">
            <!-- <text slot="message">暂无已发布的智能助手</text> -->
          </u-empty>
        </view>
        <view class="creation-items-wrapper" v-else>
          <view class="creation-item" v-for="(item, index) in agentList" :key="index" @click="viewAgentDetail(item)">
            <view class="creation-item__header">
              <view class="creation-item__avatar-name">
                <image class="creation-item__avatar" :src="item.agentAvatar || '/static/avatar/default_toy_avatar.jpeg'" mode="aspectFit"></image>
                <text class="creation-item__name">{{ item.agentName }}</text>
              </view>
              <view class="creation-item__clone-btn" @click.stop="cloneAgent(item)">
                <u-icon name="plus" color="#fff" size="32rpx"></u-icon>
              </view>
            </view>
            <view class="creation-item__content">
              <view class="creation-item__info">
                <view class="creation-item__info-item">
                  <text class="creation-item__info-label">音色模型：</text>
                  <text class="creation-item__info-value">{{ item.ttsVoiceName || '未选择' }}</text>
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
    
    <!-- 音色选择弹出层 -->
    <view v-if="showVoiceSelector" class="custom-popup-mask" @click="showVoiceSelector = false">
      <view class="custom-popup-content" @click.stop>
        <view class="voice-selector">
          <view class="voice-selector__header">
            <text class="voice-selector__title">选择音色</text>
            <view class="voice-selector__close" @click="showVoiceSelector = false">
              <u-icon name="close" size="30rpx" color="#666"></u-icon>
            </view>
          </view>
          <view class="voice-selector__body">
            <view class="voice-selector__item" @click="selectVoice('')">
              <text class="voice-selector__item-text">全部音色</text>
              <u-icon v-if="selectedVoice === ''" name="checkmark" color="#5778ff" size="32rpx"></u-icon>
            </view>
            <view 
              class="voice-selector__item" 
              v-for="(item, index) in voiceList" 
              :key="index"
              @click="selectVoice(item.name)"
            >
              <text class="voice-selector__item-text">{{ item.name }}</text>
              <view class="voice-selector__item-actions">
                <u-icon v-if="selectedVoice === item.name" name="checkmark" color="#5778ff" size="32rpx"></u-icon>
                <view class="voice-selector__item-play" @click.stop="playVoiceDemo(item)">
                  <u-icon :name="playingVoiceId === item.id ? 'pause-circle' : 'play-circle'" size="40rpx" color="#1890ff"></u-icon>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import agentApi from '@/api/module/agent';
import modelApi from '@/api/module/model';

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
      isLoading: false,
      
      // 搜索相关
      searchKeyword: '',
      selectedVoice: '',
      showVoiceSelector: false,
      voiceList: [],
      
      // 筛选条件
      filter: {
        ttsVoiceName: ''
      },
      
      // 音频播放相关
      playingVoiceId: null,
      audioContext: null
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
    
    // 初始化音频上下文
    this.audioContext = uni.createInnerAudioContext();
    this.audioContext.onEnded(() => {
      this.playingVoiceId = null;
    });
    this.audioContext.onError((res) => {
      console.error('音频播放错误', res);
      uni.showToast({
        title: '音频播放失败',
        icon: 'none'
      });
      this.playingVoiceId = null;
    });
    
    // 获取音色列表
    this.getVoiceList();
    
    // 获取智能体列表
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
  onUnload() {
    // 页面卸载时释放音频资源
    if (this.audioContext) {
      this.audioContext.destroy();
    }
  },
  methods: {
    // 获取音色列表
    async getVoiceList() {
      try {
        // 为简化，这里直接获取TTS模型的音色列表
        // 实际使用时可能需要先获取默认TTS模型ID
        const res = await modelApi.getVoiceList('TTS_HuoshanDoubleStreamTTS');
        if (res && Array.isArray(res)) {
          this.voiceList = res;
        }
      } catch (error) {
        console.error('获取音色列表失败', error);
      }
    },
    
    // 选择音色
    selectVoice(voice) {
      this.selectedVoice = voice;
      this.filter.ttsVoiceName = voice;
      this.showVoiceSelector = false;
      this.resetAndRefresh();
    },
    
    // 搜索处理
    handleSearch() {
      this.filter.ttsVoiceName = this.selectedVoice || this.searchKeyword;
      this.resetAndRefresh();
    },
    
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
        
        // 构建筛选条件
        const filterDto = { ...this.filter };
        
        // 如果筛选条件为空，则不传
        if (!filterDto.ttsVoiceName) {
          delete filterDto.ttsVoiceName;
        }
        
        // 调用API获取数据
        const res = await agentApi.getPublishedAgentList(params, Object.keys(filterDto).length > 0 ? filterDto : null);
        
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
    },
    
    // 清除搜索
    clearSearch() {
      this.searchKeyword = '';
      this.selectedVoice = '';
      this.filter.ttsVoiceName = '';
      this.resetAndRefresh();
    },
    
    // 播放音色示例音频
    playVoiceDemo(voice) {
      if (!voice.voiceDemo) {
        uni.showToast({
          title: '该音色暂无示例音频',
          icon: 'none'
        });
        return;
      }
      
      if (this.playingVoiceId === voice.id) {
        // 如果正在播放当前音频，则停止播放
        this.audioContext.stop();
        this.playingVoiceId = null;
        return;
      }
      
      // 停止当前正在播放的音频
      if (this.audioContext) {
        this.audioContext.stop();
      }
      
      // 播放新的音频
      this.playingVoiceId = voice.id;
      this.audioContext.src = voice.voiceDemo;
      this.audioContext.play();
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
  position: relative;
  color: #fff;
  flex-shrink: 0;
  padding: 40rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 160rpx);
  padding-bottom: 60rpx;
  overflow: hidden;

  &__bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
    filter: blur(3px);
    transform: scale(1.1);
  }

  &__content {
    position: relative;
    z-index: 2;
    margin-bottom: 30rpx;
  }

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

.creation-content-body {
  flex: 1;
  background-color: #f8f8f8;
  margin-top: -40rpx;
  border-radius: 40rpx 40rpx 0 0;
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 搜索区域样式 */
.creation-search {
  padding: 20rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  
  &__wrapper {
    height: 72rpx;
    background-color: #f5f7fa;
    border-radius: 36rpx;
    display: flex;
    align-items: center;
    padding: 0 20rpx;
  }
  
  &__dropdown {
    display: flex;
    align-items: center;
    padding: 0 10rpx;
    height: 100%;
    
    &-text {
      font-size: 28rpx;
      color: #333;
      margin-right: 6rpx;
    }
  }
  
  &__divider {
    width: 2rpx;
    height: 36rpx;
    background-color: #ddd;
    margin: 0 16rpx;
  }
  
  &__input {
    flex: 1;
    height: 100%;
    font-size: 28rpx;
    color: #333;
  }
  
  &__clear {
    padding: 0 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  &__icon {
    padding: 0 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.creation-list-scroll-view {
  flex: 1;
  height: 0;
  overflow-y: auto;
}

.creation-items-wrapper {
  padding: 20rpx 20rpx 0 20rpx;
}

/* 音色选择器样式 */
.voice-selector {
  background-color: #fff;
  padding-bottom: 30rpx;
  max-height: 45vh;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 100;
  
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #eee;
    position: sticky;
    top: 0;
    background-color: #fff;
    z-index: 10;
  }
  
  &__title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }
  
  &__close {
    padding: 10rpx;
    height: 50rpx;
    width: 50rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  &__body {
    max-height: calc(70vh - 100rpx);
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
    padding-bottom: env(safe-area-inset-bottom);
  }
  
  &__item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    &-text {
      font-size: 28rpx;
      color: #333;
    }
    
    &-actions {
      display: flex;
      align-items: center;
      gap: 20rpx;
    }
    
    &-play {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
    }
  }
}

.creation-items-wrapper {
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
      margin-bottom: 30rpx;
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

.custom-popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.custom-popup-content {
  width: 100%;
  background-color: #fff;
  border-radius: 20rpx 20rpx 0 0;
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}
</style> 