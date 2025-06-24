<template>
  <view class="pub-agent-page">
    
    <view class="pub-agent-content" v-if="agentInfo">
      <!-- 智能体基本信息 -->
      <view class="pub-agent-header">
        <view class="pub-agent-avatar-box">
          <image class="pub-agent-avatar" :src="agentInfo.agentAvatar || '/static/avatar/default_toy_avatar.jpeg'" mode="aspectFit"></image>
        </view>
        <view class="pub-agent-info">
          <text class="pub-agent-name">{{ agentInfo.agentName }}</text>
          <view class="pub-agent-stats">
            <view class="pub-agent-stat-item" @click="toggleLike">
              <u-icon :name="isLiked ? 'heart-fill' : 'heart'" size="40rpx" :color="isLiked ? '#FF5B8F' : '#979db1'"></u-icon>
              <text class="pub-agent-stat-text">{{ likes }}</text>
            </view>
            <view class="pub-agent-stat-item" @click="shareAgent">
              <u-icon name="share" size="40rpx" color="#979db1"></u-icon>
              <text class="pub-agent-stat-text">分享</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 智能体详细信息 -->
      <view class="pub-agent-details">
        <view class="detail-section">
          <view class="detail-title">基本信息</view>
          <view class="detail-item">
            <text class="detail-label">音色模型：</text>
            <text class="detail-value">{{ voiceName || '未设置' }}</text>
          </view>
          <view class="detail-item" v-if="agentInfo.llmModelName">
            <text class="detail-label">语言模型：</text>
            <text class="detail-value">{{ agentInfo.llmModelName }}</text>
          </view>
          <view class="detail-item" v-if="agentInfo.ttsModelName">
            <text class="detail-label">语音模型：</text>
            <text class="detail-value">{{ agentInfo.ttsModelName }}</text>
          </view>
        </view>
        
        <view class="detail-section">
          <view class="detail-title">角色介绍</view>
          <view class="detail-content">
            <text class="detail-text">{{ agentInfo.systemPrompt || '该智能体暂无介绍' }}</text>
          </view>
        </view>
        
        <view class="detail-section" v-if="agentInfo.summaryMemory">
          <view class="detail-title">记忆内容</view>
          <view class="detail-content">
            <text class="detail-text">{{ agentInfo.summaryMemory }}</text>
          </view>
        </view>
      </view>
      
      <!-- 底部操作区 -->
      <view class="pub-agent-action">
        <u-button type="primary" text="使用该智能体" @click="useAgent"></u-button>
      </view>
    </view>
    
    <!-- 加载状态 -->
    <view class="loading-overlay" v-if="isLoading">
      <u-loading-icon size="80" mode="circle"></u-loading-icon>
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script>
import agentApi from '@/api/module/agent';
import modelApi from '@/api/module/model';

export default {
  data() {
    return {
      id: '',
      agentInfo: null,
      isLoading: true,
      isLiked: false,
      likes: 0,
      voiceName: ''
    };
  },
  onLoad(options) {
    if (options.id) {
      this.id = options.id;
      this.fetchAgentInfo();
      
      // 从本地存储获取点赞状态
      try {
        const likedAgentsStr = uni.getStorageSync('likedAgents');
        if (likedAgentsStr) {
          const likedAgents = JSON.parse(likedAgentsStr);
          this.isLiked = !!likedAgents[this.id];
        }
      } catch (e) {
        console.error('获取点赞状态失败', e);
      }
      
      // 模拟点赞数
      this.likes = Math.floor(Math.random() * 1000);
    } else {
      uni.showToast({
        title: '参数错误',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  },
  methods: {
    // 获取智能体详情
    async fetchAgentInfo() {
      try {
        this.isLoading = true;
        const info = await agentApi.getAgentDetail(this.id);
        this.agentInfo = info;
        
        // 获取音色名称
        if (info.ttsModelId && info.ttsVoiceId) {
          // 判断是否为克隆音色
          if (info.ttsModelId === 'TTS_FeiShuTTS') {
            this.fetchCloneVoiceName(info.ttsVoiceId);
          } else {
            this.fetchVoiceName(info.ttsModelId, info.ttsVoiceId);
          }
        } else {
          this.voiceName = info.ttsVoiceName || '未设置';
        }
      } catch (error) {
        console.error('获取智能体详情失败', error);
        uni.showToast({
          title: '获取详情失败',
          icon: 'none'
        });
      } finally {
        this.isLoading = false;
      }
    },
    
    // 获取预设音色名称
    async fetchVoiceName(modelId, voiceId) {
      try {
        const voices = await modelApi.getVoiceList(modelId);
        const voiceList = Array.isArray(voices) ? voices : (voices.list || []);
        const voice = voiceList.find(v => v.id === voiceId || v.voiceId === voiceId);
        if (voice) {
          this.voiceName = voice.name || voice.voiceName;
        } else {
          this.voiceName = this.agentInfo.ttsVoiceName || voiceId;
        }
      } catch (error) {
        console.error('获取音色名称失败', error);
        this.voiceName = this.agentInfo.ttsVoiceName || voiceId;
      }
    },
    
    // 获取克隆音色名称
    async fetchCloneVoiceName(voiceId) {
      try {
        const voices = await modelApi.getCloneVoiceList();
        const voiceList = Array.isArray(voices) ? voices : (voices.list || []);
        const voice = voiceList.find(v => v.id === voiceId || v.voiceId === voiceId);
        if (voice) {
          this.voiceName = voice.name || voice.voiceName;
        } else {
          this.voiceName = this.agentInfo.ttsVoiceName || voiceId;
        }
      } catch (error) {
        console.error('获取克隆音色名称失败', error);
        this.voiceName = this.agentInfo.ttsVoiceName || voiceId;
      }
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack();
    },
    
    // 切换点赞状态
    toggleLike() {
      this.isLiked = !this.isLiked;
      
      // 更新点赞数
      if (this.isLiked) {
        this.likes++;
      } else {
        this.likes--;
      }
      
      // 保存点赞状态到本地存储
      try {
        const likedAgentsStr = uni.getStorageSync('likedAgents');
        let likedAgents = {};
        if (likedAgentsStr) {
          likedAgents = JSON.parse(likedAgentsStr);
        }
        
        if (this.isLiked) {
          likedAgents[this.id] = true;
        } else {
          delete likedAgents[this.id];
        }
        
        uni.setStorageSync('likedAgents', JSON.stringify(likedAgents));
      } catch (e) {
        console.error('保存点赞状态失败', e);
      }
      
      // 显示点赞反馈
      uni.showToast({
        title: this.isLiked ? '已点赞' : '已取消点赞',
        icon: 'none'
      });
    },
    
    // 分享智能体
    shareAgent() {
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
    
    // 使用该智能体
    async useAgent() {
        try {
            uni.showLoading({
                title: '使用中...',
                mask: true
            });
            
            const result = await agentApi.cloneAgent(this.id);
            
            uni.hideLoading();
            uni.showToast({
                title: '使用成功',
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
            console.error('使用智能体失败', error);
            uni.showToast({
                title: '使用失败: ' + (error.msg || '未知错误'),
                icon: 'none',
                duration: 3000
            });
        }
    }
  }
};
</script>

<style lang="scss">
.pub-agent-page {
  min-height: 100vh;
  background-color: #f8f8f8;
  position: relative;
}

.nav-back {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 90rpx;
  background-color: #fff;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  z-index: 100;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.pub-agent-content {
  padding: 30rpx;
}

.pub-agent-header {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .pub-agent-avatar-box {
    margin-right: 30rpx;
  }
  
  .pub-agent-avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background-color: #f0f0f0;
    border: 2rpx solid #eaeaea;
  }
  
  .pub-agent-info {
    flex: 1;
  }
  
  .pub-agent-name {
    font-size: 40rpx;
    font-weight: bold;
    color: #3d4566;
    margin-bottom: 20rpx;
    display: block;
  }
  
  .pub-agent-stats {
    display: flex;
    gap: 30rpx;
  }
  
  .pub-agent-stat-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }
  
  .pub-agent-stat-text {
    font-size: 24rpx;
    color: #979db1;
  }
}

.pub-agent-details {
  margin-bottom: 120rpx; /* 为底部按钮预留空间 */
  
  .detail-section {
    background-color: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 30rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  }
  
  .detail-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #3d4566;
    margin-bottom: 20rpx;
    position: relative;
    padding-left: 20rpx;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 10rpx;
      width: 8rpx;
      height: 30rpx;
      background: linear-gradient(135deg, #5778ff, #6b8aff);
      border-radius: 4rpx;
    }
  }
  
  .detail-item {
    display: flex;
    margin-bottom: 16rpx;
  }
  
  .detail-label {
    font-size: 28rpx;
    color: #666;
    // width: 180rpx;
  }
  
  .detail-value {
    font-size: 28rpx;
    color: #333;
    font-weight: bold;
  }
  
  .detail-content {
    background-color: #f9f9f9;
    border-radius: 12rpx;
    padding: 20rpx;
    max-height: 580rpx;
    overflow-y: auto;
  }
  
  .detail-text {
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
    word-break: break-all;
    white-space: pre-wrap;
  }
}

.pub-agent-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom)); /* 兼容iPhone底部安全区 */
  z-index: 100;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.loading-text {
  font-size: 28rpx;
  color: #666;
  margin-top: 20rpx;
}
</style>
  
  