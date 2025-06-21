<template>
  <view class="device-page">
    <!-- 搜索区域 -->
    <view class="search-area">
      <u-search
        v-model="searchKeyword"
        placeholder="请输入设备型号或Mac地址"
        shape="round"
        clearabled
        :show-action="true"
        action-text="搜索 "
        @search="handleSearch"
        @custom="handleSearch"
      ></u-search>
    </view>
    
    <!-- 设备列表 -->
    <scroll-view 
      class="device-list"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 设备卡片 -->
      <view v-if="filteredDeviceList.length > 0">
        <view 
          class="device-card"
          v-for="(device, index) in paginatedDeviceList" 
          :key="index"
        >
          <view class="card-header">
            <view class="model-info">
              <u-icon name="server-fill" size="40rpx" color="#5f70f3"></u-icon>
              <text class="model-text">{{getFirmwareTypeName(device.model)}}</text>
            </view>
            <view class="card-header-right">
              <u-button 
                type="error" 
                size="default"
                @click="handleUnbind(device.device_id)"
                :custom-style="{height: '60rpx', width: '120rpx', lineHeight: '58rpx', padding: '0', fontSize: '32rpx', borderRadius: '30rpx'}"
                text="解绑"
              ></u-button>
            </view>
          </view>
          
          <view class="card-body">
            <view class="info-row">
              <text class="info-label">固件版本：</text>
              <text class="info-value">{{device.firmwareVersion || '暂无'}}</text>
            </view>
            <!-- <view class="info-row">
              <text class="info-label">Mac地址：</text>
              <text class="info-value">{{device.macAddress || '暂无'}}</text>
            </view> -->
            <view class="info-row">
              <text class="info-label">绑定时间：</text>
              <text class="info-value">{{device.bindTime || '暂无'}}</text>
            </view>
            <view class="info-row">
              <text class="info-label">最近对话：</text>
              <text class="info-value">{{device.lastConversation || '暂无'}}</text>
            </view>
            <view class="info-row remark-row">
              <text class="info-label">备注：</text>
              <view class="remark-content" v-if="!device.isEdit" @tap="startEditRemark(device)">
                <text class="remark-text">{{device.remark || '点击添加备注'}}</text>
                <u-icon name="edit-pen" size="28rpx" color="#979db1" style="flex-shrink: 0;"></u-icon>
              </view>
              <view class="remark-edit" v-else>
                <u-input
                  v-model="device.remark"
                  type="text"
                  placeholder="请输入备注信息"
                  maxlength="64"
                  :border="true"
                  :custom-style="{height: '60rpx', lineHeight: '60rpx', marginLeft: '10rpx'}"
                  @blur="onRemarkBlur(device)"
                  @confirm="onRemarkEnter(device)"
                ></u-input>
              </view>
            </view>
            <view class="info-row switch-row">
              <text class="info-label">自动升级：</text>
              <view class="switch-value">
                <u-switch 
                  v-model="device.otaSwitch"
                  :size="switchSize"
                  activeColor="#5bc98c"
                  inactiveColor="#e6e6e6"
                  @change="(value) => handleOtaSwitchChange(device, value)"
                ></u-switch>
              </view>
            </view>
          </view>
          
          <!-- <view class="card-footer"> -->
          <!-- </view> -->
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-else class="empty-state">
        <u-empty
          mode="list"
          icon=""
          text="暂无设备数据"
        ></u-empty>
      </view>
      
      <!-- 加载状态 -->
      <view class="loading-more" v-if="loading && hasMoreData">
        <u-loading-icon mode="circle" color="#5f70f3"></u-loading-icon>
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 加载完毕 -->
      <view class="no-more" v-if="!hasMoreData && paginatedDeviceList.length > 0">
        <text class="no-more-text">— 没有更多设备了 —</text>
      </view>
    </scroll-view>
    
    <!-- 底部悬浮按钮 -->
    <view class="floating-button" @click="handleAddDevice">
      <u-icon name="plus" color="#ffffff" size="60rpx"></u-icon>
    </view>
    
    <!-- 添加设备弹窗 -->
    <u-popup 
      v-model:show="addDeviceDialogVisible"
      mode="center"
      width="80%"
      height="auto"
      :customStyle="{borderRadius: '16rpx'}"
      :z-index="10090"
    >
      <view class="add-device-popup">
        <view class="popup-header">
          <text class="popup-title">添加设备</text>
          <u-icon name="close" size="30rpx" @click="addDeviceDialogVisible = false"></u-icon>
        </view>
        <view class="popup-body">
          <u-form :model="addDeviceForm" ref="addDeviceForm">
            <u-form-item label="验证码：" prop="deviceCode" labelWidth="160rpx">
              <u-input 
                v-model="addDeviceForm.deviceCode" 
                placeholder="输入设备播报的6位验证码"
                maxlength="6"
                type="number"
                :customStyle="{backgroundColor: '#f5f6fa'}"
              ></u-input>
            </u-form-item>
          </u-form>
        </view>
        <view class="popup-footer">
          <u-button type="primary" text="确定" @click="confirmAddDevice" :loading="isBinding"></u-button>
          <u-button type="info" text="取消" plain @click="addDeviceDialogVisible = false" :disabled="isBinding"></u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import deviceApi from '@/api/module/device';
import dictApi from '@/api/module/dict';

export default {
  data() {
    return {
      // 搜索关键词
      searchKeyword: "",
      activeSearchKeyword: "",
      
      // 设备列表数据
      deviceList: [],
      filteredDeviceList: [],
      
      // 加载和分页
      loading: false,
      refreshing: false,
      currentPage: 1,
      pageSize: 10,
      hasMoreData: true,
      
      // OTA开关状态提示
      otaSwitchTips: {
        on: '自动升级已开启',
        off: '自动升级已关闭'
      },
      
      // 设备信息
      deviceInfo: {
        screenWidth: 375,
        platform: ''
      },
      
      // 开关尺寸
      switchSizeValue: 24,
      
      // 新增设备
      addDeviceDialogVisible: false,
      addDeviceForm: {
        deviceCode: ''
      },
      isBinding: false,
      
      // 固件类型字典
      firmwareTypes: [],
      
      // 智能体ID
      agentId: null
    }
  },
  computed: {
    // 当前分页数据
    paginatedDeviceList() {
      return this.filteredDeviceList.slice(0, this.currentPage * this.pageSize);
    },
    
    // 根据设备尺寸计算合适的开关尺寸
    switchSize() {
      // 平板等大屏设备使用更大的尺寸
      if (this.deviceInfo.screenWidth > 768) {
        return 36;
      } else if (this.deviceInfo.screenWidth > 500) {
        // 中等尺寸设备
        return 28;
      } else {
        // 普通手机设备
        return 24;
      }
    }
  },
  onLoad(options) {
    if (options.id) {
      this.agentId = options.id;
      this.fetchBindDevices();
    } else {
      uni.showToast({
        title: '缺少智能体ID',
        icon: 'none'
      });
    }
    this.getFirmwareTypes();
    this.getDeviceInfo();
  },
  methods: {
    // 获取设备信息
    getDeviceInfo() {
      try {
        const systemInfo = uni.getSystemInfoSync();
        this.deviceInfo.screenWidth = systemInfo.screenWidth;
        this.deviceInfo.platform = systemInfo.platform;
        console.log('设备信息:', systemInfo);
      } catch (e) {
        console.error('获取设备信息失败', e);
      }
    },
    
    // 搜索设备
    handleSearch() {
      this.activeSearchKeyword = this.searchKeyword.trim().toLowerCase();
      this.filterDevices();
      this.currentPage = 1;
    },
    
    // 筛选设备列表
    filterDevices() {
      const keyword = this.activeSearchKeyword;
      if (!keyword) {
        this.filteredDeviceList = [...this.deviceList];
      } else {
        this.filteredDeviceList = this.deviceList.filter(device => 
          (device.model && device.model.toLowerCase().includes(keyword)) ||
          (device.macAddress && device.macAddress.toLowerCase().includes(keyword))
        );
      }
      
      // 重置更多数据标识
      this.hasMoreData = this.filteredDeviceList.length > this.pageSize;
    },
    
    // 加载更多
    loadMore() {
      if (!this.hasMoreData || this.loading) return;
      
      this.loading = true;
      setTimeout(() => {
        this.currentPage++;
        this.hasMoreData = this.filteredDeviceList.length > this.currentPage * this.pageSize;
        this.loading = false;
      }, 500);
    },
    
    // 下拉刷新
    onRefresh() {
      this.refreshing = true;
      this.fetchBindDevices(() => {
        this.refreshing = false;
      });
    },
    
    // 获取绑定设备列表
    fetchBindDevices(callback) {
      this.loading = true;
      
      // 使用页面传递的智能体ID而不是从本地存储获取
      const agentId = this.agentId;
      
      if (!agentId) {
        uni.showToast({
          title: '缺少智能体ID',
          icon: 'none'
        });
        this.loading = false;
        return;
      }
      
      // 先清空列表，确保UI能感知到变化
      this.deviceList = [];
      this.filteredDeviceList = [];
      
      deviceApi.getUserDevices(agentId)
        .then(res => {
            this.deviceList = (res || []).map(device => {
              return {
                device_id: device.id,
                model: device.board,
                firmwareVersion: device.appVersion,
                macAddress: device.macAddress,
                bindTime: device.createDate,
                lastConversation: device.lastConnectedAt,
                remark: device.alias,
                _originalRemark: device.alias,
                isEdit: false,
                _submitting: false,
                otaSwitch: device.autoUpdate === 1,
                rawBindTime: new Date(device.createDate).getTime()
              };
            }).sort((a, b) => a.rawBindTime - b.rawBindTime);
            
            this.filterDevices();
            this.currentPage = 1;
            this.hasMoreData = this.filteredDeviceList.length > this.pageSize;
            
            // 强制下一个渲染周期更新视图
            this.$nextTick(() => {
              console.log('设备列表已更新，数量:', this.deviceList.length);
            });
        })
        .catch(err => {
          uni.showToast({
            title: err.message || '获取设备列表失败',
            icon: 'none'
          });
        })
        .finally(() => {
          this.loading = false;
          callback && callback();
        });
    },
    
    // 获取固件类型
    getFirmwareTypes() {
      // 从字典API获取固件类型数据
      dictApi.getDictDataByType('FIRMWARE_TYPE')
        .then(res => {
          this.firmwareTypes = (res || []).map(item => {
            return {
              key: item.key,
              name: item.name
            };
          });
          console.log('获取固件类型成功:', this.firmwareTypes);
        })
        .catch(err => {
          console.error('获取固件类型失败:', err);
          // 设置默认值，避免页面显示异常
          this.firmwareTypes = [
            { key: 'ESP32', name: 'ESP32设备' },
            { key: 'ESP8266', name: 'ESP8266设备' }
          ];
        });
    },
    
    // 获取固件类型名称
    getFirmwareTypeName(type) {
      const firmwareType = this.firmwareTypes.find(item => item.key === type);
      return firmwareType ? firmwareType.name : type;
    },
    
    // 开始编辑备注
    startEditRemark(device) {
      device.isEdit = true;
    },
    
    // 提交备注
    submitRemark(device) {
      if (device._submitting) return;
      
      const text = (device.remark || '').trim();
      
      if (text.length > 64) {
        uni.showToast({
          title: '备注不能超过64个字符',
          icon: 'none'
        });
        device.remark = device._originalRemark;
        return;
      }
      
      if (text === device._originalRemark) {
        return;
      }
      
      device._submitting = true;
      
      // 调用更新设备信息API
      deviceApi.updateDeviceInfo(device.device_id, { alias: text })
        .then(() => {
          device._originalRemark = text;
          uni.showToast({
            title: '备注已保存',
            icon: 'success'
          });
        })
        .catch(err => {
          device.remark = device._originalRemark;
          uni.showToast({
            title: err.message || '备注保存失败',
            icon: 'none'
          });
        })
        .finally(() => {
          device._submitting = false;
        });
    },
    
    // 备注输入框：失焦时提交
    onRemarkBlur(device) {
      device.isEdit = false;
      setTimeout(() => {
        this.submitRemark(device);
      }, 100); // 延迟100ms，避开enter+blur同时触发的窗口
    },
    
    // 备注输入框：按回车时提交
    onRemarkEnter(device) {
      device.isEdit = false;
      this.submitRemark(device);
    },
    
    // 完成编辑备注 (兼容原有方法)
    finishEditRemark(device) {
      this.onRemarkBlur(device);
    },
    
    // 处理OTA开关变化
    handleOtaSwitchChange(device, newValue) {
      // 先更新本地UI状态，避免点击后没有响应
      device.otaSwitch = newValue;
      
      deviceApi.updateDeviceInfo(device.device_id, { autoUpdate: newValue ? 1 : 0 })
        .then(() => {
          uni.showToast({
            title: newValue ? '已设置成自动升级' : '已关闭自动升级',
            icon: 'success'
          });
        })
        .catch(err => {
          // 如果操作失败，恢复到原来的状态
          device.otaSwitch = !newValue;
          uni.showToast({
            title: err.message || '操作失败',
            icon: 'none'
          });
        });
    },
    
    // 解绑单个设备
    handleUnbind(deviceId) {
      uni.showModal({
        title: '警告',
        content: '确认要解绑该设备吗？',
        success: res => {
          if (res.confirm) {
            deviceApi.unbindDevice({ deviceId: deviceId })
              .then(() => {
                // 解绑成功，直接显示成功提示并刷新列表
                uni.showToast({
                  title: '设备解绑成功',
                  icon: 'success'
                });
                // 刷新设备列表
                this.fetchBindDevices();
              })
              .catch(err => {
                uni.showToast({
                  title: err.message || '设备解绑失败',
                  icon: 'none'
                });
              });
          }
        }
      });
    },
    
    // 添加设备
    handleAddDevice() {
      console.log('点击添加设备按钮');
      this.addDeviceDialogVisible = true;
    },
    
    // 确认添加设备
    confirmAddDevice() {
      if (!this.addDeviceForm.deviceCode) {
        uni.showToast({
          title: '请输入验证码',
          icon: 'none'
        });
        return;
      }
      
      if (this.addDeviceForm.deviceCode.length !== 6) {
        uni.showToast({
          title: '验证码必须为6位数',
          icon: 'none'
        });
        return;
      }
      
      this.isBinding = true;
      
      // 调用绑定设备API
      deviceApi.bindDevice(this.agentId, this.addDeviceForm.deviceCode)
        .then(() => {
          // 绑定成功，直接显示成功提示并刷新列表
          uni.showToast({
            title: '设备绑定成功',
            icon: 'success'
          });
          this.addDeviceDialogVisible = false;
          this.addDeviceForm.deviceCode = '';
          // 刷新设备列表
          this.fetchBindDevices();
        })
        .catch(err => {
          uni.showToast({
            title: err.message || '设备绑定失败',
            icon: 'none'
          });
        })
        .finally(() => {
          this.isBinding = false;
        });
    }
  }
}
</script>

<style lang="scss">
.device-page {
  height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
  position: relative;
}

.search-area {
  padding: 24rpx 30rpx;
  background-color: #ffffff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  margin-bottom: 16rpx;
}

.device-list {
  flex: 1;
  padding: 20rpx 30rpx 0 30rpx;
  box-sizing: border-box;
}

.device-card {
  background: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  width: 100%;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20rpx;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  margin: 0 -10rpx 10rpx -10rpx;
  padding: 0 10rpx 20rpx 10rpx;
}

.card-header-right {
  display: flex;
  align-items: center;
}

.model-info {
  display: flex;
  align-items: center;
  height: 60rpx;
  background-color: rgba(95, 112, 243, 0.1);
  padding: 6rpx 16rpx;
  border-radius: 30rpx;
}

.model-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-left: 12rpx;
  max-width: 250rpx;  /* 限制宽度，避免过长 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-body {
  padding: 20rpx 0;
  overflow: hidden;
  margin-top: 10rpx;
}

.info-row {
  display: flex;
  margin-bottom: 16rpx;
  align-items: center;
  padding: 8rpx 0;
}

.info-label {
  width: 170rpx;
  font-size: 30rpx;
  color: #666;
  flex-shrink: 0;
  line-height: 44rpx;
  font-weight: bold;
}

.info-value {
  font-size: 30rpx;
  color: #333;
  flex: 1;
  line-height: 44rpx;
  word-break: break-all;
}

.remark-row {
  align-items: center;
}

.remark-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60rpx;
  background-color: #f8f8f8;
  border-radius: 8rpx;
  padding: 0 10rpx;
}

.remark-text {
  font-size: 30rpx;
  color: #333;
  flex: 1;
  line-height: 44rpx;
  padding: 10rpx;
}

.remark-edit {
  flex: 1;
  height: 60rpx;
  display: flex;
  align-items: center;
}

.switch-row {
  margin-top: 10rpx;
}

.switch-value {
  flex: 1;
  display: flex;
  align-items: center;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  height: 10rpx;
  margin: 10rpx -10rpx 0 -10rpx;
  padding: 20rpx 10rpx 0 10rpx;
}

.empty-state {
  padding: 100rpx 0;
}

.loading-more, .no-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 30rpx 0;
}

.loading-text, .no-more-text {
  font-size: 24rpx;
  color: #999;
  margin-left: 10rpx;
}

.floating-button {
  position: fixed;
  /* 移除右侧定位，改为水平居中 */
  left: 50%;
  transform: translateX(-50%);
  bottom: 120rpx;
  width: 130rpx;
  height: 130rpx;
  background: linear-gradient(135deg, #4cd964, #00c16e);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 193, 110, 0.5);
  z-index: 100;
  transition: all 0.3s ease;
  
  /* 添加平台适配 */
  /* #ifdef MP-WEIXIN */
  bottom: 140rpx; /* 微信小程序环境 */
  /* #endif */
  
  /* #ifdef H5 */
  bottom: 180rpx; /* H5环境 */
  /* #endif */
  
  &:active {
    /* 正确结合缩放和水平居中效果 */
    transform: translateX(-50%) scale(0.9);
    box-shadow: 0 3rpx 10rpx rgba(0, 193, 110, 0.3);
  }
}

.add-device-popup {
  padding: 30rpx;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.popup-body {
  margin-bottom: 30rpx;
}

.popup-footer {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
  
  .u-button {
    flex: 1;
  }
}

/* 主题色调整 */
::v-deep .u-search {
  background: #f5f6fa;
}

::v-deep .u-search__content {
  background: #ffffff;
}


::v-deep .u-button--success {
  background: #5bc98c;
}

::v-deep .u-button--error {
  background: #fd5b63;
}
</style>
