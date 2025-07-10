<template>
  <view class="agent-page">
    <view class="agent-header">
      <image class="agent-header__bg" src="https://jh-vioce.oss-cn-shanghai.aliyuncs.com/image/4f9baf7e_ban1.png" mode="aspectFill"></image>
      <view class="agent-header__content">
        <!-- <image class="agent-header__avatar" src="/static/avatar/default_toy_avatar_old.jpeg" mode="aspectFit"></image> -->
        <view class="agent-header__info">
          <text class="agent-header__name">飞鼠智能体</text>
          <text class="agent-header__description">一个专为儿童打造的温馨互动乐园！在这里，孩子们将遇见专属的AI玩伴，一同探索、学习和成长。</text>
        </view>
        <view class="agent-header__config-btn" @click="goToFirstAgentConfig">
          <text>角色定义</text>
        </view>
      </view>
    </view>

    <view class="agent-content-body">
      <view class="add-device-section">
        <text class="add-device-section__prompt">快添加新设备开始聊天吧~</text>
        <u-button class="add-device-section__btn" type="primary" shape="circle" @click="showAddDialog">
          <u-icon name="plus" color="#ffffff" size="28rpx"></u-icon>
          <text>新设备</text>
        </u-button>
      </view>

      <view class="list-title-wrapper">
        <text class="list-title">智能体列表</text>
      </view>

      <scroll-view
        class="agent-list-scroll-view"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view v-if="agentList.length > 0">
          <view class="agent-items">
            <view class="agent-item" v-for="(item, index) in agentList" :key="index">
              <view class="agent-item__header">
                <view class="agent-item__avatar-name">
                  <image class="agent-item__avatar" :src="item.agentAvatar || '/static/avatar/default_toy_avatar.jpeg'" mode="aspectFit"></image>
                  <text class="agent-item__name">{{ item.agentName }}</text>
                </view>
                <view class="agent-item__actions">
                  <u-icon name="trash" size="54rpx" color="#FF5B8F" @click.stop="confirmDelete(item)"></u-icon>
                </view>
              </view>
              <view class="agent-item__content">
                <view class="agent-item__info">
                  <view class="agent-item__info-item">
                    <text class="agent-item__info-label">音色模型：</text>
                    <text class="agent-item__info-value">{{ item.ttsVoiceName || '未选择' }}</text>
                  </view>
                  <view class="agent-item__info-item publish-switch-row">
                    <text class="agent-item__info-label">发布状态：</text>
                    <view class="publish-switch-value">
                      <u-switch
                        v-model="item.published"
                        :size="24"
                        activeColor="#5bc98c"
                        inactiveColor="#e6e6e6"
                        @change="(value) => handlePublishSwitchChange(item, value)"
                      ></u-switch>
                    </view>
                  </view>
                </view>
              </view>
              <view class="agent-item__button-group">
                <view class="agent-item__button" @click="goToConfig(item)">
                  <text>角色配置</text>
                </view>
                <view class="agent-item__button" @click="goToDeviceManage(item)">
                  <text>设备管理({{ item.deviceCount || 0 }})</text>
                </view>
                <view class="agent-item__button" :class="{'agent-item__button--disabled': item.memModelId === 'Memory_nomem'}" @click="goToChatHistory(item)">
                  <text>聊天记录</text>
                </view>
              </view>
              <view class="agent-item__footer">
                <text class="agent-item__time" v-if="item.lastConnectedAt">最近连接: {{ $filters.formatDate(item.lastConnectedAt, 'YYYY-MM-DD HH:mm') }}</text>
              </view>
            </view>
            <view style="height: 30rpx;"></view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 添加智能体对话框 -->
    <u-popup :show="addDialogVisible" mode="center" round="10" @close="closeAddDialog">
      <view class="add-dialog">
        <view class="add-dialog__header">
          <text class="add-dialog__title">添加智能体</text>
          <u-icon name="close" size="28rpx" @click="closeAddDialog"></u-icon>
        </view>
        <view class="add-dialog__content">
          <u-form :model="agentForm" ref="agentForm">
            <u-form-item label="助手昵称" prop="agentName" labelWidth="150rpx">
              <u-input v-model="agentForm.agentName" placeholder="请输入助手昵称" maxlength="10"></u-input>
            </u-form-item>
          </u-form>
        </view>
        <view class="add-dialog__footer">
          <u-button type="primary" text="确定" @click="createAgent"></u-button>
          <u-button type="info" text="取消" plain @click="closeAddDialog"></u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import agentApi from '@/api/module/agent';

export default {
  data() {
    return {
      agentList: [],
      addDialogVisible: false,
      agentForm: {
        agentName: '',
        ttsVoiceId: 'TTS_DoubaoTTS0001',
        ttsModelId: 'TTS_DoubaoTTS',
        vadModelId: 'VAD_SileroVAD',
        asrModelId: 'ASR_DoubaoASR',
        llmModelId: 'LLM_AliLLM',
        memModelId: 'Memory_mem0ai',
        intentModelId: 'Intent_function_call',
        chatHistoryConf: 1,
        langCode: 'zh',
        language: '中文',
        published: 0
      },
      refreshing: false
    };
  },
  onLoad() {
    this.getAgentList();
  },
  onShow() {
    // 每次页面显示时刷新数据
    this.getAgentList();
  },
  onPullDownRefresh() {
    this.getAgentList();
    uni.stopPullDownRefresh();
  },
  methods: {
    // 获取智能体列表
    async getAgentList() {
      try {
        uni.showLoading({
          title: '加载中...',
          mask: true
        });
        const list = await agentApi.getAgentList();
        this.agentList = list.map(item => {
          return {
            ...item,
            published: item.published === 1
          };
        });
      } catch (error) {
        console.error('获取智能体列表失败', error);
        uni.showToast({
          title: '获取列表失败',
          icon: 'none'
        });
      } finally {
        uni.hideLoading();
        // 如果是下拉刷新触发的，结束刷新状态
        if (this.refreshing) {
          this.refreshing = false;
        }
      }
    },
    // 显示添加对话框
    showAddDialog() {
      this.addDialogVisible = true;
      this.agentForm.agentName = '';
    },
    
    // 关闭添加对话框
    closeAddDialog() {
      this.addDialogVisible = false;
    },
    
    // 创建智能体
    createAgent() {
      if (!this.agentForm.agentName) {
        uni.showToast({
          title: '请输入助手昵称',
          icon: 'none'
        });
        return;
      }
      
      // 显示加载中
      uni.showLoading({
        title: '创建中...'
      });
      
      // 调用创建API
      agentApi.createAgent({agentName: this.agentForm.agentName}).then(res => {
        uni.hideLoading();
        uni.showToast({
          title: '创建成功',
          icon: 'success'
        });
        
        // 关闭对话框
        this.closeAddDialog();
        
        // 刷新列表
        this.getAgentList();
      }).catch(error => {
        uni.hideLoading();
        console.error('创建智能体失败', error);
        uni.showToast({
          title: '创建失败',
          icon: 'none'
        });
      });
    },
    // 跳转到配置页面
    goToConfig(item) {
      uni.navigateTo({
        url: `/pages/agent/config?id=${item.id}`
      });
    },
    // 跳转到设备管理页面
    goToDeviceManage(item) {
      uni.navigateTo({
        url: `/pages/agent/device?id=${item.id}`
      });
    },
    // 跳转到聊天记录页面
    goToChatHistory(item) {
      if (item.memModelId === 'Memory_nomem') {
        uni.showToast({
          title: '请先在角色配置中开启记忆',
          icon: 'none'
        });
        return;
      }
      uni.navigateTo({
        url: `/pages/agent/chat-history?id=${item.id}&name=${encodeURIComponent(item.agentName)}`
      });
    },
    // 确认删除
    confirmDelete(item) {
      uni.showModal({
        title: '提示',
        content: `确定要删除"${item.agentName}"吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await agentApi.deleteAgent(item.id);
              uni.showToast({
                title: '删除成功',
                icon: 'success'
              });
              this.getAgentList();
            } catch (error) {
              console.error('删除失败', error);
              uni.showToast({
                title: '删除失败',
                icon: 'none'
              });
            }
          }
        }
      });
    },
    // 处理发布状态开关变化
    handlePublishSwitchChange(item, newValue) {
      // 先更新本地UI状态，避免点击后没有响应
      item.published = newValue;

      const published = newValue ? 1 : 0;
      
      // 调用更新API，只更新published字段
      agentApi.updateAgent(item.id, { published: published })
        .then(() => {
          uni.showToast({
            title: newValue ? '已发布' : '已取消发布',
            icon: 'success'
          });
        })
        .catch(err => {
          // 如果操作失败，恢复到原来的状态
          item.published = !newValue;
          uni.showToast({
            title: err.message || '操作失败',
            icon: 'none'
          });
        });
    },
    // 跳转到第一个智能体的配置
    goToFirstAgentConfig() {
      if (this.agentList.length > 0) {
        this.goToConfig(this.agentList[0]);
      } else {
        uni.showToast({
          title: '请先创建智能体',
          icon: 'none'
        });
      }
    },
    // 下拉刷新
    onRefresh() {
      this.refreshing = true;
      this.getAgentList();
    }
  }
};
</script>

<style lang="scss">
.agent-page {
  height: 100vh;
  background-color: #f8f8f8;
  position: relative;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.agent-header {
  position: relative;
  color: #fff;
  flex-shrink: 0;
  padding: 40rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 160rpx);
  padding-bottom: 100rpx;
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
    display: flex;
    align-items: flex-end;
  }

  &__avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.5);
    flex-shrink: 0;
  }

  &__info {
    margin-left: 20rpx;
    flex: 1;
  }

  &__name {
    font-size: 40rpx;
    font-weight: bold;
    display: block;
  }

  &__description {
    font-size: 24rpx;
    opacity: 0.9;
    margin-top: 10rpx;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__config-btn {
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 30rpx;
    padding: 10rpx 20rpx;
    font-size: 24rpx;
    flex-shrink: 0;
    margin-left: 20rpx;
    text-align: center;
  }
}

.agent-content-body {
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

.add-device-section {
  padding: 40rpx 30rpx;
  text-align: center;
  background-color: #fff;
  flex-shrink: 0;

  &__prompt {
    font-size: 28rpx;
    color: #666;
    display: block;
    margin-bottom: 30rpx;
  }

  &__btn {
    width: 300rpx;
    height: 80rpx;

    .u-icon {
      margin-right: 10rpx;
    }
  }
}

.agent-list-scroll-view {
  flex: 1;
  height: 0;
}

.list-title-wrapper {
  padding: 40rpx 30rpx 40rpx 30rpx;
}
.list-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.agent-items {
  padding: 0 30rpx;
  .agent-item {
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

    &__actions {
      display: flex;
      align-items: center;
    }

    &__content {
      margin-bottom: 30rpx;
      margin-left: -20rpx;
    }

    &__info {
      &-item {
        display: flex;
        margin-bottom: 16rpx;
      }

      &-label {
        font-size: 28rpx;
        color: #666;
        width: 180rpx;
        text-align: right;
      }

      &-value {
        font-size: 28rpx;
        color: #333;
        font-weight: bold;
      }
    }

    &__button-group {
      display: flex;
      gap: 20rpx;
      margin-bottom: 20rpx;
    }

    &__button {
      background-color: #e6ebff;
      color: #5778ff;
      font-size: 26rpx;
      font-weight: 500;
      padding: 10rpx 20rpx;
      border-radius: 28rpx;
      text-align: center;

      &--disabled {
        background-color: #e6e6e6;
        color: #999;
      }
    }

    &__footer {
      display: flex;
      justify-content: flex-end;
    }

    &__time {
      font-size: 24rpx;
      color: #979db1;
    }
  }
}

// 覆盖u-popup默认样式，防止占用空间
:deep(.u-popup) {
  &:not(.u-popup--show) {
    flex: 0 !important;
    height: 0 !important;
    min-height: 0 !important;
    overflow: hidden !important;
  }
}

// 添加对话框样式
.add-dialog {
  width: 600rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 10rpx;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
  }
  
  &__title {
    font-size: 32rpx;
    font-weight: bold;
    color: #3d4566;
  }
  
  &__content {
    margin-bottom: 30rpx;
  }
  
  &__footer {
    display: flex;
    gap: 20rpx;
    
    .u-button {
      flex: 1;
    }
  }
}

// 发布状态开关样式
.publish-switch-row {
  margin-top: 10rpx;
  align-items: center;
}

.publish-switch-value {
  flex: 1;
  display: flex;
  align-items: center;
}
</style> 