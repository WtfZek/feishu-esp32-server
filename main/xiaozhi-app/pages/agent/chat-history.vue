<template>
  <view class="chat-history-page">
    <view class="chat-header">
      <!-- <view class="back-button" @click="goBack">
        <u-icon name="arrow-left" size="40rpx" color="#333"></u-icon>
      </view> -->
      <view class="header-title">
        <text>与{{ agentName }}的聊天记录</text>
        <text v-if="currentMacAddress" class="device-info">[{{ currentMacAddress }}]</text>
      </view>
    </view>
    
    <view class="chat-container">
      <!-- 聊天内容区域 - 放在最底层 -->
      <view class="chat-content">
        <scroll-view 
          scroll-y 
          class="messages-scroll"
          :scroll-top="scrollTop"
          :scroll-with-animation="true"
        >
          <view v-if="currentSessionId" class="messages">
            <view v-for="(message, index) in messagesWithTime" :key="message.id || index">
              <view v-if="message.type === 'time'" class="time-divider">
                {{ message.content }}
              </view>
              <view v-else class="message-item" :class="{ 'user-message': message.chatType === 1 || message.chatType === '1' }">
                <image 
                  :src="(message.chatType === 1 || message.chatType === '1') ? getUserAvatar(currentSessionId) : agentAvatar || '/static/avatar/default_toy_avatar.jpeg'" 
                  class="avatar"
                  mode="aspectFit"
                />
                <view class="message-content">
                  <view v-if="message.audioId" class="audio-icon" @click="playAudio(message)">
                    <u-icon :name="playingAudioId === message.audioId ? 'pause-circle' : 'play-circle'" size="40rpx" :color="(message.chatType === 1 || message.chatType === '1') ? '#fff' : '#1890ff'"></u-icon>
                  </view>
                  {{ message.content }}
                </view>
              </view>
            </view>
          </view>
          <view v-else class="no-session-selected">
            <text>请选择会话查看聊天记录</text>
          </view>
        </scroll-view>
      </view>
      
      <!-- 侧边栏会话列表，可折叠 - 放在上层 -->
      <view class="sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }" ref="sidebar">
        <scroll-view 
          class="session-list" 
          scroll-y 
          @scrolltolower="loadMoreSessions"
          v-if="!sidebarCollapsed"
        >
          <view 
            v-for="session in sessions" 
            :key="session.sessionId" 
            class="session-item"
            :class="{ active: currentSessionId === session.sessionId }" 
            @click="selectSession(session)"
          >
            <image :src="getUserAvatar(session.sessionId)" class="avatar" />
            <view class="session-info">
              <view class="session-info-row">
                <text class="session-time">{{ $filters.formatDate(session.createdAt, 'YYYY-MM-DD HH:mm') }}</text>
                <view class="message-count">{{ session.chatCount > 99 ? '99+' : session.chatCount }}</view>
              </view>
            </view>
          </view>
          <view v-if="loading" class="loading">
            <text>加载中...</text>
          </view>
          <view v-if="!hasMore && sessions.length > 0" class="no-more">没有更多记录了</view>
          <view v-if="sessions.length === 0 && !loading" class="empty-list">
            <u-empty mode="list" icon=""></u-empty>
          </view>
        </scroll-view>
      </view>
      
      <!-- 侧边栏切换按钮 - 放在最上层 -->
      <view 
        class="sidebar-toggle" 
        :class="{ 'toggle-collapsed': sidebarCollapsed }" 
        @click="toggleSidebar"
        ref="toggleButton"
        :style="{ left: toggleLeft + 'px' }"
      >
        <u-icon :name="sidebarCollapsed ? 'arrow-right' : 'arrow-left'" size="40rpx" color="#333"></u-icon>
      </view>
    </view>
  </view>
</template>

<script>
import agentApi from '@/api/module/agent';
import http from '@/api/http';

export default {
  data() {
    return {
      agentId: '',
      agentName: '',
      agentAvatar: '',
      sessions: [],
      messages: [],
      currentSessionId: '',
      currentMacAddress: '',
      page: 1,
      limit: 20,
      loading: false,
      hasMore: true,
      sidebarCollapsed: false,
      scrollTop: 0,
      playingAudioId: null,
      audioContext: null,
      // 新增：用于动态绑定按钮的 left 值
      toggleLeft: 180 // 给一个初始值，防止闪烁
    };
  },
  // 使用 onReady 是因为它能确保DOM已经渲染完成
  onReady() {
    this.updateTogglePosition();
  },
  onLoad(options) {
    this.agentId = options.id || '';
    this.agentName = decodeURIComponent(options.name || '');
    this.agentAvatar = decodeURIComponent(options.avatar || '');
    
    // 初始化音频上下文
    this.audioContext = uni.createInnerAudioContext();
    this.audioContext.onEnded(() => {
      this.playingAudioId = null;
    });
    
    this.loadSessions();
  },
  onUnload() {
    // 页面卸载时释放音频资源
    if (this.audioContext) {
      this.audioContext.destroy();
    }
  },
  computed: {
    messagesWithTime() {
      if (!this.messages || this.messages.length === 0) return [];

      const result = [];
      const TIME_INTERVAL = 60 * 1000; // 1分钟的时间间隔（毫秒）

      // 添加第一条消息的时间标记
      if (this.messages[0]) {
        result.push({
          type: 'time',
          content: this.$filters.formatDate(this.messages[0].createdAt, 'YYYY-MM-DD HH:mm'),
          id: `time-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
        });
      }

      // 处理消息列表
      for (let i = 0; i < this.messages.length; i++) {
        const currentMessage = this.messages[i];
        result.push(currentMessage);

        // 检查是否需要添加时间标记
        if (i < this.messages.length - 1) {
          const currentTime = new Date(currentMessage.createdAt).getTime();
          const nextTime = new Date(this.messages[i + 1].createdAt).getTime();

          if (nextTime - currentTime > TIME_INTERVAL) {
            result.push({
              type: 'time',
              content: this.$filters.formatDate(this.messages[i + 1].createdAt, 'YYYY-MM-DD HH:mm'),
              id: `time-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
            });
          }
        }
      }

      return result;
    }
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    // 新增：更新折叠按钮位置的方法
    updateTogglePosition() {
      // 使用 uni-app 的 createSelectorQuery API 获取元素信息
      const query = uni.createSelectorQuery().in(this);
      query.select('.sidebar').boundingClientRect(data => {
        if (data && data.width) {
          // 获取侧边栏的实际宽度，并设置给 toggleLeft
          this.toggleLeft = data.width;
        }
      }).exec();
    },
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed;
      // 切换后，如果需要按钮位置变化（虽然现在CSS处理了），也可以在这里重新计算
      // if (!this.sidebarCollapsed) {
      //   this.$nextTick(() => {
      //     this.updateTogglePosition();
      //   });
      // }
    },
    async loadSessions() {
      if (this.loading || (!this.page === 1 && !this.hasMore)) {
        return;
      }

      this.loading = true;
      const params = {
        page: this.page,
        limit: this.limit
      };

      uni.showLoading({
        title: '加载中...',
        mask: true
      });

      try {
        const data = await agentApi.getAgentSessions(this.agentId, params);
        if (data && data.list) {
          const list = data.list;
          this.hasMore = list.length === this.limit;

          if (this.page === 1) {
            this.sessions = list;
          } else {
            this.sessions = [...this.sessions, ...list];
          }
          
          this.page++;

          if (this.sessions.length > 0 && !this.currentSessionId) {
            this.selectSession(this.sessions[0]);
          }
          this.$nextTick(() => {
            this.updateTogglePosition();
          });
        }
      } catch (error) {
        console.error('获取会话列表失败', error);
        uni.showToast({
          title: '获取会话列表失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
        uni.hideLoading();
      }
    },
    loadMoreSessions() {
      if (!this.loading && this.hasMore) {
        this.loadSessions();
      }
    },
    async selectSession(session) {
      this.currentSessionId = session.sessionId;
      uni.showLoading({
        title: '加载中...',
        mask: true
      });
      
      try {
        const data = await agentApi.getAgentChatHistory(this.agentId, session.sessionId);
        if (data) {
          this.messages = data;
          if (this.messages.length > 0 && this.messages[0].macAddress) {
            this.currentMacAddress = this.messages[0].macAddress;
          }
          
          // 在小程序中，需要设置延时才能正确滚动到底部
          setTimeout(() => {
            this.scrollToBottom();
          }, 300);
        }
      } catch (error) {
        console.error('获取聊天记录失败', error);
        uni.showToast({
          title: '获取聊天记录失败',
          icon: 'none'
        });
      } finally {
        uni.hideLoading();
      }
      
      // 在小屏幕设备上选择会话后自动折叠侧边栏
      if (uni.getSystemInfoSync().windowWidth < 768) {
        this.sidebarCollapsed = true;
      }
    },
    scrollToBottom() {
      // 使用一个随机大数来确保滚动到底部
      this.scrollTop = 100000;
    },
    async playAudio(message) {
      if (this.playingAudioId === message.audioId) {
        // 如果正在播放当前音频，则停止播放
        this.audioContext.stop();
        this.playingAudioId = null;
        return;
      }

      // 停止当前正在播放的音频
      if (this.audioContext) {
        this.audioContext.stop();
      }

      // 先获取音频下载ID
      this.playingAudioId = message.audioId;
      try {
        const audioId = await agentApi.getAudioId(message.audioId);
        if (audioId) {
          // 使用获取到的下载ID播放音频
          const audioUrl = http.baseUrl + `/agent/play/${audioId}`;
          this.audioContext.src = audioUrl;
          this.audioContext.play();
        }
      } catch (error) {
        console.error('获取音频失败', error);
        this.playingAudioId = null;
        uni.showToast({
          title: '音频播放失败',
          icon: 'none'
        });
      }
    },
    getUserAvatar(sessionId) {
      // 从 sessionId 中提取所有数字
      const numbers = sessionId.match(/\d+/g);
      if (!numbers) return '/static/avatar/default_toy_avatar.jpeg';

      // 将所有数字相加
      const sum = numbers.reduce((acc, num) => acc + parseInt(num), 0);

      // 计算模5并加1，得到1-5之间的数字
      const avatarIndex = (sum % 5) + 1;

      // 返回对应的头像图片
      return `/static/avatar/user-avatar${avatarIndex}.png`;
    },
  }
};
</script>

<style lang="scss">
.chat-history-page {
  min-height: 100vh;
  background-color: #f8f8f8;
  display: flex;
  flex-direction: column;
}

.chat-header {
  height: 88rpx;
  background-color: #e6f7ff;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  position: relative;
  z-index: 10;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .back-button {
    padding: 10rpx;
  }
  
  .header-title {
    flex: 1;
    text-align: center;
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    
    .device-info {
      font-size: 24rpx;
      color: #666;
      margin-left: 10rpx;
      font-weight: normal;
    }
  }
}

.chat-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.chat-content {
  width: 100%;
  height: 100%;
  background-color: #f5f6fa;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  
  .messages-scroll {
    height: 100%;
    box-sizing: border-box;
    padding: 30rpx;
  }
  
  .messages {
    padding-bottom: 40rpx;
  }
  
  .message-item {
    display: flex;
    margin-bottom: 30rpx;
    
    &.user-message {
      flex-direction: row-reverse;
      
      .message-content {
        background-color: #1890ff;
        color: white;
        
        .audio-icon {
          margin-right: 20rpx;
          margin-left: 0;
          order: -1; /* 在用户消息中，将音频图标放在开头 */
        }
      }
      
      .avatar {
        margin-left: 20rpx;
        margin-right: 0;
      }
    }
    
    .avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      margin-right: 20rpx;
      flex-shrink: 0;
    }
    
    .message-content {
      max-width: 70%;
      padding: 20rpx 30rpx;
      border-radius: 16rpx;
      background-color: #fff;
      position: relative;
      font-size: 28rpx;
      line-height: 1.5;
      display: flex;
      align-items: center;
      
      .audio-icon {
        margin-left: 20rpx;
        cursor: pointer;
      }
    }
  }
  
  .time-divider {
    text-align: center;
    margin: 30rpx 0;
    color: #999;
    font-size: 24rpx;
    position: relative;
    
    &::before, &::after {
      content: '';
      display: inline-block;
      width: 20%;
      height: 1rpx;
      background-color: #eee;
      vertical-align: middle;
      margin: 0 20rpx;
    }
  }
  
  .no-session-selected {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #999;
    font-size: 28rpx;
  }
}

.sidebar {
  width: auto;
  min-width: 180px;
  height: 100%;
  background-color: #fff;
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 5;
  box-shadow: 2rpx 0 10rpx rgba(0, 0, 0, 0.05);
  
  &-collapsed {
    width: 0;
    min-width: 0;
    overflow: hidden;
  }
  
  .session-list {
    height: 100%;
    padding: 20rpx 10rpx;
    box-sizing: border-box;
    
    .session-item {
      display: flex;
      flex-direction: row;
      align-items: center;
      padding: 20rpx 10rpx;
      border-radius: 16rpx;
      margin-bottom: 20rpx;
      width: 100%;
      box-sizing: border-box;
      
      &:hover, &.active {
        background-color: #f0f7ff;
      }
      
      &.active {
        border-left: 6rpx solid #1890ff;
      }
      
      .avatar {
        width: 70rpx;
        height: 70rpx;
        border-radius: 50%;
        margin-right: 10rpx;
        flex-shrink: 0;
      }
      
      .session-info {
        flex: 1;
        min-width: 0; /* 确保flex子项可以正确收缩 */
        
        &-row {
          display: flex;
          flex-direction: row;
          justify-content: space-between;
          align-items: center;
          width: 100%;
        }
        
        .session-time {
          font-size: 24rpx;
          color: #272727;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          flex: 1;
          margin-right: 10rpx;
          min-width: 0; /* 确保文本可以被截断 */
        }
        
        .message-count {
          font-size: 24rpx;
          color: #fff;
          background-color: #b4b4b4;
          border-radius: 20rpx;
          display: inline-block;
          min-width: 40rpx;
          height: 40rpx;
          line-height: 40rpx;
          text-align: center;
          flex-shrink: 0;
        }
      }
    }
    
    .loading, .no-more, .empty-list {
      text-align: center;
      padding: 20rpx;
      color: #999;
      font-size: 24rpx;
    }
  }
}

.sidebar-toggle {
  position: absolute;
  top: 50%; /* 垂直居中 */
  transform: translateY(-50%); /* 确保真正居中 */
  width: 55rpx;
  height: 120rpx; /* 增加高度 */
  background-color: #fff;
  border-radius: 0 8rpx 8rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  box-shadow: 2rpx 0 5rpx rgba(0, 0, 0, 0.1);
  
  &.toggle-collapsed {
    left: 0 !important; // 使用 !important 确保覆盖行内样式
  }
}

/* 响应式布局 */
@media screen and (max-width: 768rpx) {
  .sidebar {
    position: absolute;
    height: 100%;
    z-index: 100;
  }
}
</style>
