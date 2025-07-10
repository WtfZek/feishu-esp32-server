<template>
  <view class="voice-page">
    <!-- 页面头部 -->
    <view class="voice-header">
      <image class="voice-header__bg" src="https://jh-vioce.oss-cn-shanghai.aliyuncs.com/image/d151a6cf_ban3.png" mode="aspectFill"></image>
      <view class="voice-header__content">
        <text class="voice-title">音色库</text>
        <text class="voice-subtitle">体验多种音色，打造专属声音</text>
      </view>
    </view>
    
    <view class="voice-content-body">
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
        class="voice-list-scroll-view"
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
        <view class="voice-items-wrapper" v-else>
          <!-- 音色列表项 -->
          <view
            v-for="(item, index) in voiceList"
            :key="item.id"
            class="voice-item"
          >
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

          <!-- 底部预留高度 -->
          <view style="height: 30rpx;"></view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 克隆音色按钮，固定在内容区下方 -->
    <view class="voice-add">
      <u-button type="primary" text="克隆新音色" @click="goToClone" background="linear-gradient(135deg, #5778ff, #6b8aff);" shape="circle">
        <u-icon name="plus" color="#ffffff" size="28rpx"></u-icon>
        <text>克隆新音色</text>
      </u-button>
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

.voice-content-body {
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

.voice-list-scroll-view {
  flex: 1;
  height: 0;
  overflow-y: auto;
}

.voice-items-wrapper {
  padding: 20rpx 20rpx 0 20rpx;
}

.voice-empty {
  padding: 100rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.voice-items-wrapper {
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

.voice-add {
  position: relative;
  padding: 40rpx 30rpx;
  text-align: center;
  background-color: #fff;
  z-index: 10;
  border-radius: 40rpx 40rpx 0 0;
  margin-top: -40rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  
  .u-button {
    width: 300rpx;
    height: 80rpx;

    .u-icon {
      margin-right: 10rpx;
    }
  }
}

// 使用CSS选择器给第一个音色项添加上边距
.voice-items-wrapper .voice-item:first-child {
  margin-top: 10rpx;
}
</style>
  
  