<template>
  <view class="feedback-page">
    
    <view class="feedback-content">
      <view class="form-container">
        <u-form :model="form" ref="uForm">
          <u-form-item label="反馈类型：" labelWidth="160rpx" prop="type">
            <u-radio-group v-model="form.type">
              <u-radio :name="1" label="问题反馈"></u-radio>
              <u-radio :name="2" label="功能建议"></u-radio>
              <u-radio :name="3" label="其他"></u-radio>
            </u-radio-group>
          </u-form-item>
          
          <u-form-item label="反馈内容：" labelWidth="160rpx" prop="content">
            <u-textarea v-model="form.content" placeholder="请输入反馈内容" count maxlength="500" height="300" :customStyle="{backgroundColor: '#f5f6fa'}"></u-textarea>
          </u-form-item>
          
          <u-form-item label="联系方式：" labelWidth="160rpx" prop="contact">
            <u-input v-model="form.contact" placeholder="请输入您的联系方式（选填）" :customStyle="{backgroundColor: '#f5f6fa'}"></u-input>
          </u-form-item>
        </u-form>
        
        <view class="form-actions">
          <u-button type="primary" text="提交反馈" @click="submitFeedback"></u-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      form: {
        type: 1,
        content: '',
        contact: ''
      }
      // 暂不添加逻辑
    }
  }
}
</script>

<style lang="scss">
.feedback-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.feedback-content {
  flex: 1;
  padding: 30rpx;
}

.form-container {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
}

.form-actions {
  margin-top: 40rpx;
}
</style>
