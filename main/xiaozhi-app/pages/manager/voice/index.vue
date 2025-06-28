<template>
  <view class="voice-page">
    <!-- 页面头部 -->
    <view class="voice-header">
      <text class="voice-title">音色库</text>
      <text class="voice-subtitle">体验多种音色，打造专属声音</text>
    </view>
    
    <!-- 音色类型切换 -->
    <view class="voice-type-switch">
      <view 
        class="voice-type-item" 
        :class="{'voice-type-active': voiceType === 'preset'}"
        @click="switchVoiceType('preset')"
      >预设音色</view>
      <view 
        class="voice-type-item" 
        :class="{'voice-type-active': voiceType === 'clone'}"
        @click="switchVoiceType('clone')"
      >我的克隆</view>
    </view>
    
    <!-- 音色列表 -->
    <scroll-view 
      class="voice-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="voice-empty" v-if="voiceList.length === 0">
        <u-empty mode="list" icon="">
          <text slot="message">{{ voiceType === 'preset' ? '暂无预设音色' : '暂无克隆音色' }}</text>
        </u-empty>
      </view>
      <view class="voice-items" v-else>
        <!-- 音色列表项 -->
        <view 
          v-for="(item, index) in voiceList" 
          :key="item.id"
          class="voice-item"
        >
        <!-- @click="handleVoiceItemClick(item)" -->
          <view class="voice-item__header">
            <view class="voice-item__name">{{ item.name || item.voiceName }}</view>
            <view class="voice-item__play" @click="playVoiceDemo(item)">
              <u-icon :name="playingVoiceId === (item.id || item.voiceId) ? 'pause-circle' : 'play-circle'" size="60rpx" color="#1890ff"></u-icon>
            </view>
          </view>
          <view class="voice-item__content">
            <!-- <view class="voice-item__desc" v-if="item.desc">{{ item.desc }}</view> -->
            <view class="voice-item__info-row">
              <view class="voice-item__label" v-if="item.languages">语言：{{ item.languages }}</view>
              <view class="voice-item__label" v-if="voiceType === 'clone' && (item.createDate || item.createTime)">
                创建时间：{{ $filters.formatDate(item.createDate || item.createTime, 'YYYY-MM-DD HH:mm') }}
              </view>
            </view>
          </view>
        </view>
        
        <!-- 底部预留高度，避免浮动按钮遮挡内容 -->
        <view style="height: 130rpx;"></view>
      </view>
    </scroll-view>
    
    <!-- 克隆音色按钮，固定在底部 -->
    <view class="voice-add">
      <u-button type="primary" text="克隆新音色" @click="goToClone" background="linear-gradient(135deg, #5778ff, #6b8aff);"></u-button>
    </view>
  </view>
</template>

<script>
import modelApi from '@/api/module/model';
import Router from '@/utils/router';

export default {
  data() {
    return {
      voiceType: 'preset', // 默认显示预设音色
      voiceList: [],
      refreshing: false,
      isLoading: false,
      
      // 音频播放相关
      playingVoiceId: null,
      audioContext: null
    };
  },
  onLoad() {
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
  },
  onShow() {
    // 每次页面显示时刷新数据
    this.getVoiceList();
  },
  onUnload() {
    // 页面卸载时释放音频资源
    if (this.audioContext) {
      this.audioContext.destroy();
    }
  },
  methods: {
    // 切换音色类型
    switchVoiceType(type) {
      if (this.voiceType === type) return;
      
      this.voiceType = type;
      
      // 重新获取音色列表
      this.getVoiceList();
      
      // 停止当前正在播放的音频
      if (this.audioContext) {
        this.audioContext.stop();
        this.playingVoiceId = null;
      }
    },
    
    // 获取音色列表
    async getVoiceList() {
      if (this.isLoading) return;
      
      try {
        this.isLoading = true;
        
        uni.showLoading({
          title: '加载中...',
          mask: true
        });
        
        let res;
        if (this.voiceType === 'preset') {
          // 获取预设音色列表
          res = await modelApi.getVoiceList('TTS_HuoshanDoubleStreamTTS');
        } else {
          // 获取克隆音色列表
          res = await modelApi.getCloneVoiceList();
        }
        
        // 处理音色列表数据
        if (res && Array.isArray(res)) {
          this.voiceList = res;
        } else if (res && res.list && Array.isArray(res.list)) {
          this.voiceList = res.list;
        } else {
          this.voiceList = [];
        }
        
      } catch (error) {
        console.error('获取音色列表失败', error);
        uni.showToast({
          title: '获取列表失败',
          icon: 'none'
        });
        this.voiceList = [];
      } finally {
        this.isLoading = false;
        uni.hideLoading();
        
        // 如果是下拉刷新触发的，结束刷新状态
        if (this.refreshing) {
          this.refreshing = false;
        }
      }
    },
    
    // 下拉刷新
    onRefresh() {
      this.refreshing = true;
      this.getVoiceList();
    },
    
    // 播放音色示例音频
    playVoiceDemo(voice) {
      const voiceDemo = voice.voiceDemo || '';
      
      if (!voiceDemo) {
        uni.showToast({
          title: '该音色暂无示例音频',
          icon: 'none'
        });
        return;
      }
      
      const voiceId = voice.id || voice.voiceId;
      
      if (this.playingVoiceId === voiceId) {
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
      this.playingVoiceId = voiceId;
      this.audioContext.src = voiceDemo;
      this.audioContext.play();
    },
    
    // 跳转到克隆音色页面
    goToClone() {
        Router.navigateTo(Router.ROUTES.VOICE.CLONE);
    }
  }
};
</script>

<style lang="scss">
.voice-page {
  width: 100%;
  height: 100vh;
  background-color: #f8f8f8;
  position: relative;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.voice-header {
  padding: 30rpx;
  background: linear-gradient(135deg, #5778ff, #6b8aff);
  color: #fff;
  flex-shrink: 0;
  
  .voice-title {
    font-size: 44rpx;
    font-weight: bold;
    margin-bottom: 10rpx;
    display: block;
  }
  
  .voice-subtitle {
    font-size: 28rpx;
    opacity: 0.9;
  }
}

.voice-type-switch {
  display: flex;
  padding: 20rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
  justify-content: center;
}

.voice-type-item {
  padding: 14rpx 40rpx;
  font-size: 28rpx;
  border-radius: 30rpx;
  background-color: #f5f6fa;
  color: #666;
  margin-right: 20rpx;
  
  &.voice-type-active {
    background-color: #5778ff;
    color: #ffffff;
  }
}

.voice-list {
  flex: 1;
  padding: 20rpx 20rpx 0 20rpx;
  box-sizing: border-box;
  width: 100%;
  height: 0;
  overflow: hidden;
  
  .voice-empty {
    padding: 100rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
  
  .voice-items {
    .voice-item {
      background-color: #fff;
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 30rpx;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
      
      &__header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        // margin-bottom: 20rpx;
      }
      
      &__name {
        font-size: 32rpx;
        font-weight: bold;
        color: #3d4566;
      }
      
      &__play {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      &__content {
        margin-top: 20rpx;
      }
      
      &__info-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10rpx;
      }
      
      &__desc {
        font-size: 28rpx;
        color: #666;
        margin-bottom: 16rpx;
        line-height: 1.5;
      }
      
      &__label {
        font-size: 26rpx;
        color: #979db1;
      }
    }
  }
}

.voice-add {
  position: fixed;
  bottom: 40rpx;
  left: 0;
  right: 0;
  padding: 0 40rpx;
  z-index: 10;
  transition: bottom 0.3s;
  
  .u-button {
    box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.1);
  }
}

// 使用CSS选择器给第一个音色项添加上边距
.voice-items .voice-item:first-child {
  margin-top: 10rpx;
}
</style>
  
  