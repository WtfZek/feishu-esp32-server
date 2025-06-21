<template>
  <view class="config-page">
    
    <view class="config-content">
      <view class="config-header">
        <view class="config-hint">
          <u-icon name="info-circle" size="30rpx" color="#979db1"></u-icon>
          <text class="hint-text">保存配置后，需要重启设备，新的配置才会生效</text>
        </view>
      </view>
      
      <view class="config-form">
        <u-form :model="form" ref="uForm" :rules="rules">
          <u-form-item label="助手昵称：" prop="agentName" labelWidth="160rpx">
            <u-input v-model="form.agentName" placeholder="请输入助手昵称" maxlength="10" :customStyle="{backgroundColor: '#f5f6fa'}"></u-input>
          </u-form-item>
          
          <view class="template-section">
            <view class="section-title">角色模板：</view>
            <view class="template-list">
              <view
                v-for="(template, index) in templates"
                :key="index"
                class="template-item"
                :class="{'template-item-loading': loadingTemplate}"
                @click="selectTemplate(template)"
              >
                {{ template.agentName }}
              </view>
            </view>
          </view>
          
          <view class="model-section">
          <view class="model-item">
		      <text class="model-label">角色音色：</text>
              <!-- 添加音色类型切换 -->
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
		      <view class="model-selector">
		        <picker :range="voiceOptions" range-key="label" @change="handleVoiceChange">
		          <view class="picker-view">
		            <text>{{ getSelectedVoiceName() }}</text>
		            <u-icon name="arrow-right" size="28rpx" color="#999"></u-icon>
		          </view>
		        </picker>
		      </view>
		    </view>
		    <!-- <view class="section-title">模型配置</view> -->
		    
		    <!-- <view v-for="(model, index) in models" :key="index" class="model-item">
              <text class="model-label">{{ model.label }}</text>
              <view class="model-selector">
                <picker :range="getOptionsArray(model.type)" range-key="label" @change="(e) => handlePickerChange(model.type, e)">
                  <view class="picker-view">
                    <text>{{ getSelectedModelName(model.type, model.key) }}</text>
                    <u-icon name="arrow-right" size="28rpx" color="#999"></u-icon>
                  </view>
                </picker>
              </view>
              
              <view v-if="model.type === 'Memory' && form.model.memModelId !== 'Memory_nomem'" class="chat-history-options">
                <u-radio-group v-model="form.chatHistoryConf">
                  <u-radio :name="1" label="上报文字"></u-radio>
                  <u-radio :name="2" label="上报文字+语音"></u-radio>
                </u-radio-group>
              </view>
		  
		      
		    </view> -->
		    
		    
            </view>
            
            <view class="model-item">
            <text class="model-label">角色介绍：</text>
            <u-textarea
              v-model="form.systemPrompt"
              placeholder="请输入角色介绍"
              count
              maxlength="2000"
              :height="form.model.memModelId === 'Memory_mem_local_short' ? '240rpx' : '260rpx'"
              :customStyle="{backgroundColor: '#f5f6fa', borderRadius: '10rpx'}"
            ></u-textarea>
          </view>
          
          <!-- 记忆内容 -->
          <view v-if="form.model.memModelId === 'Memory_mem_local_short'" class="model-item">
            <text class="model-label">记忆内容：</text>
            <u-textarea
              v-model="form.summaryMemory"
              placeholder="请输入记忆信息"
              count
              maxlength="2000"
              height="240rpx"
              :customStyle="{backgroundColor: '#f5f6fa', borderRadius: '10rpx'}"
            ></u-textarea>
          </view>

          
        </u-form>
      </view>
      
      <view class="loading-overlay" v-if="isLoading">
        <u-loading-icon size="80" mode="circle"></u-loading-icon>
        <text class="loading-text">加载中...</text>
      </view>
      </view>
      
    <!-- 固定在底部的操作栏 -->
    <view class="bottom-bar">
      <view class="bottom-bar-content">
        <u-button type="primary" text="保存配置" @click="saveConfig"></u-button>
        <u-button type="info" text="重置" plain @click="resetConfig"></u-button>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import agentApi from '@/api/module/agent';
import modelApi from '@/api/module/model';

export default {
  data() {
    return {
      id: '',
      isEdit: false,
      isLoading: false,
      form: {
        agentName: '',
        ttsVoiceId: '',
        chatHistoryConf: 1,
        systemPrompt: '',
        summaryMemory: '',
        langCode: 'zh',
        language: '中文',
        sort: 0,
        model: {
          ttsModelId: '',
          vadModelId: '',
          asrModelId: '',
          llmModelId: '',
          memModelId: '',
          intentModelId: '',
        }
      },
      rules: {
        agentName: [
          { required: true, message: '请输入助手昵称', trigger: 'blur' }
        ]
      },
      models: [
        { label: '语音活动检测(VAD)', key: 'vadModelId', type: 'VAD' },
        { label: '语音识别(ASR)', key: 'asrModelId', type: 'ASR' },
        { label: '大语言模型(LLM)', key: 'llmModelId', type: 'LLM' },
        { label: '意图识别(Intent)', key: 'intentModelId', type: 'Intent' },
        { label: '记忆(Memory)', key: 'memModelId', type: 'Memory' },
        { label: '语音合成(TTS)', key: 'ttsModelId', type: 'TTS' },
      ],
      modelOptions: {
        VAD: [],
        ASR: [],
        LLM: [],
        Intent: [],
        Memory: [],
        TTS: []
      },
      voiceOptions: [],
      templates: [],
      loadingTemplate: false,
      voiceType: 'preset'
    }
  },
  onLoad(options) {
    console.log('页面加载，参数:', options);
    if (options.id) {
      this.id = options.id;
      this.isEdit = true;
      this.fetchAgentConfig();
    } else {
      // 初始化默认值
      this.initDefaultValues();
    }
    this.fetchModelOptions();
    this.fetchTemplates();
  },
  methods: {
    // 初始化默认值
    initDefaultValues() {
      this.form.model = {
        ttsModelId: 'TTS_DoubaoTTS',
        vadModelId: 'VAD_SileroVAD',
        asrModelId: 'ASR_DoubaoASR',
        llmModelId: 'LLM_AliLLM',
        memModelId: 'Memory_mem0ai',
        intentModelId: 'Intent_function_call',
      };
      this.form.ttsVoiceId = 'TTS_DoubaoTTS0001';
    },
    
    // 获取智能体配置
    fetchAgentConfig() {
      this.isLoading = true;
      
      console.log('开始获取智能体配置，ID:', this.id);
      
      agentApi.getAgentDetail(this.id).then(res => {
        console.log('获取到智能体配置:', res);
        // 填充表单
        const data = res;
        this.form = {
          agentName: data.agentName || '',
          ttsVoiceId: data.ttsVoiceId || '',
          chatHistoryConf: data.chatHistoryConf || 1,
          systemPrompt: data.systemPrompt || '',
          summaryMemory: data.summaryMemory || '',
          langCode: data.langCode || 'zh',
          language: data.language || '中文',
          sort: data.sort || 0,
          model: {
            ttsModelId: data.ttsModelId || '',
            vadModelId: data.vadModelId || '',
            asrModelId: data.asrModelId || '',
            llmModelId: data.llmModelId || '',
            memModelId: data.memModelId || '',
            intentModelId: data.intentModelId || '',
          }
        };
        
        // 关键修改：根据ttsModelId判断是否切换到"我的克隆"模式
        if (data.ttsModelId === 'TTS_FeiShuTTS') {
          this.voiceType = 'clone'; // 切换到"我的克隆"
        } else {
          this.voiceType = 'preset'; // 保持"预设音色"
        }

        console.log('voiceType:', this.voiceType);
        
        this.switchVoiceType(this.voiceType);
        
        // 获取音色选项
        if (data.ttsModelId) {
          this.fetchVoiceOptions(data.ttsModelId);
        }
        
        this.isLoading = false;

        this.$forceUpdate();
      }).catch(err => {
        console.error('获取智能体配置失败', err);
        this.isLoading = false;
        
        // 初始化默认值
        this.initDefaultValues();
        
        uni.showToast({
          title: '获取配置失败，已加载默认值',
          icon: 'none'
        });
      });
    },
    
    // 获取模型选项
    fetchModelOptions() {
      console.log('开始获取模型选项');
      
      // 设置默认模型选项，以防API失败
      this.setDefaultModelOptions();
      
      this.models.forEach(model => {
        // 获取每种类型的模型列表
        console.log(`正在获取${model.type}模型列表`);
        modelApi.getModelNames({ modelType: model.type, enabled: 1 }).then(res => {
          console.log(`获取到${model.type}模型列表:`, res);
          if (res && Array.isArray(res) && res.length > 0) {
            // 设置选项列表格式
            const options = res.map(item => ({
              value: item.id,
              label: item.modelName
            }));
            
            // 设置模型选项
            this.modelOptions[model.type] = options;
            
            // 如果是编辑模式且当前模型类型是TTS，则获取音色列表
            if (this.isEdit && model.type === 'TTS' && this.form.model.ttsModelId) {
              this.fetchVoiceOptions(this.form.model.ttsModelId);
            }
          }
        }).catch(err => {
          console.error(`获取${model.type}模型列表失败`, err);
          // 已设置默认选项，此处不需要额外处理
        });
      });
    },
    
    // 设置默认模型选项
    setDefaultModelOptions() {
      this.modelOptions = {
        TTS: [{ value: 'TTS_DoubaoTTS', label: '豆包TTS' }],
        LLM: [{ value: 'LLM_AliLLM', label: '阿里大模型' }],
        ASR: [{ value: 'ASR_DoubaoASR', label: '豆包ASR' }],
        VAD: [{ value: 'VAD_SileroVAD', label: 'SileroVAD' }],
        Memory: [
          { value: 'Memory_mem0ai', label: 'mem0ai' },
          { value: 'Memory_nomem', label: '无记忆' }
        ],
        Intent: [{ value: 'Intent_function_call', label: '函数调用' }]
      };
      
      // 设置默认音色选项
      this.voiceOptions = [
        { value: 'TTS_DoubaoTTS0001', label: '默认音色' }
      ];
    },
    
    // 获取音色选项
    fetchVoiceOptions(modelId) {
      // 在克隆模式下，不需要modelId
      if (this.voiceType === 'clone') {
        modelId = null;
      }
      
      if (!modelId && this.voiceType === 'preset') {
        this.voiceOptions = [];
        return;
      }
      
      if (this.voiceType === 'preset') {
        // 获取预设音色
      modelApi.getVoiceList(modelId).then(res => {
        console.log('获取到音色列表:', res);
        const voices = Array.isArray(res) ? res : (res.list || []);
        if (voices.length > 0) {
          this.voiceOptions = voices.map(voice => ({
            value: voice.id || voice.voiceId,
            label: voice.name || voice.voiceName
          }));
          
          // 如果没有选择音色且有可用音色，选择第一个
          if (!this.form.ttsVoiceId && this.voiceOptions.length > 0) {
            this.form.ttsVoiceId = this.voiceOptions[0].value;
          }
        } else {
          // 设置默认音色
          this.voiceOptions = [
            { value: 'TTS_DoubaoTTS0001', label: '默认音色' }
          ];
          if (!this.form.ttsVoiceId) {
            this.form.ttsVoiceId = 'TTS_DoubaoTTS0001';
          }
        }
      }).catch(err => {
        console.error('获取音色列表失败', err);
        // 设置默认音色
        this.voiceOptions = [
          { value: 'TTS_DoubaoTTS0001', label: '默认音色' }
        ];
        if (!this.form.ttsVoiceId) {
          this.form.ttsVoiceId = 'TTS_DoubaoTTS0001';
        }
      });
      } else {
        // 获取克隆音色
        modelApi.getCloneVoiceList().then(res => {
          console.log('获取到克隆音色列表:', res);
          const voices = Array.isArray(res) ? res : (res.list || []);
          if (voices.length > 0) {
            this.voiceOptions = voices.map(voice => ({
              value: voice.id || voice.voiceId,
              label: voice.name || voice.voiceName
            }));
            
            // 如果没有选择音色且有可用音色，选择第一个
            if (!this.form.ttsVoiceId && this.voiceOptions.length > 0) {
              this.form.ttsVoiceId = this.voiceOptions[0].value;
            }
          } else {
            this.voiceOptions = [
              { value: '', label: '暂无克隆音色' }
            ];
          }
        }).catch(err => {
          console.error('获取克隆音色列表失败', err);
          this.voiceOptions = [
            { value: '', label: '获取克隆音色失败' }
          ];
        });
      }
    },
    
    // 获取模板列表
    fetchTemplates() {
      agentApi.getAgentTemplates().then(res => {
        console.log('获取到模板列表:', res);
        this.templates = res || [];
      }).catch(err => {
        console.error('获取模板列表失败', err);
        this.templates = [];
      });
    },
    
    // 获取选项数组
    getOptionsArray(type) {
      return this.modelOptions[type] || [];
    },
    
    // 获取已选模型名称
    getSelectedModelName(type, key) {
      const modelId = this.form.model[key];
      if (!modelId) return '请选择';
      
      const options = this.modelOptions[type] || [];
      const selected = options.find(item => item.value === modelId);
      return selected ? selected.label : modelId;
    },
    
    // 获取已选音色名称
    getSelectedVoiceName() {
      const voiceId = this.form.ttsVoiceId;
      if (!voiceId) return '请选择';
      
      const selected = this.voiceOptions.find(item => item.value === voiceId);
      return selected ? selected.label : voiceId;
    },
    
    // 处理选择器变更
    handlePickerChange(type, e) {
      const index = e.detail.value;
      const options = this.getOptionsArray(type);
      if (options.length > index) {
        const selected = options[index];
        
        // 查找对应的模型字段
        const model = this.models.find(m => m.type === type);
        if (model) {
          this.form.model[model.key] = selected.value;
          
          // 如果是TTS模型，获取音色列表
          if (type === 'TTS') {
            this.form.ttsVoiceId = '';
            this.fetchVoiceOptions(selected.value);
          }
          
          // 如果是Memory模型，处理聊天历史配置
          if (type === 'Memory') {
            if (selected.value === 'Memory_nomem') {
              this.form.chatHistoryConf = 0;
            } else if (this.form.chatHistoryConf === 0) {
              this.form.chatHistoryConf = 1;
            }
          }
        }
      }
    },
    
    // 处理音色变更
    handleVoiceChange(e) {
      const index = e.detail.value;
      if (this.voiceOptions.length > index) {
        this.form.ttsVoiceId = this.voiceOptions[index].value;
      }
    },
    
    // 选择模板
    selectTemplate(template) {
      if (this.loadingTemplate) return;
      
      // 先弹出提示框
      uni.showModal({
        title: '提示',
        content: '切换角色模板后之前的角色配置数据将不会保留',
        success: (res) => {
          if (res.confirm) {
            this.applyTemplate(template);
          }
        }
      });
    },
    
    // 应用模板
    applyTemplate(template) {
      this.loadingTemplate = true;
      
      try {
        console.log('应用模板:', template);
        
        // 如果当前在"我的克隆"模式，切回"预设音色"模式
        if (this.voiceType === 'clone') {
          this.voiceType = 'preset';
        }
        
        // 只更新角色介绍和相关角色音色
        const currentForm = { ...this.form };
        
        // 更新表单，只改变角色介绍和角色音色
        this.form = {
          ...currentForm,
          agentName: template.agentName || currentForm.agentName,
          systemPrompt: template.systemPrompt || currentForm.systemPrompt,
          // 只更新TTS模型ID，音色ID在获取音色列表后设置
          model: {
            ...currentForm.model,
            ttsModelId: template.ttsModelId || currentForm.model.ttsModelId
          }
        };
        
        // 获取对应的音色列表
        if (template.ttsModelId) {
          // 清空当前音色ID，避免显示错误的音色
          this.form.ttsVoiceId = '';
          
          // 获取新的音色列表并设置模板的音色ID
          modelApi.getVoiceList(template.ttsModelId).then(res => {
            console.log('获取到音色列表:', res);
            const voices = Array.isArray(res) ? res : (res.list || []);
            if (voices.length > 0) {
              this.voiceOptions = voices.map(voice => ({
                value: voice.id || voice.voiceId,
                label: voice.name || voice.voiceName
              }));
              
              // 设置为模板指定的音色或第一个可用音色
              if (template.ttsVoiceId) {
                // 检查模板的音色是否在列表中
                const voiceExists = this.voiceOptions.some(voice => voice.value === template.ttsVoiceId);
                if (voiceExists) {
                  this.form.ttsVoiceId = template.ttsVoiceId;
                } else {
                  // 如果找不到对应音色，选择第一个
                  console.log('找不到对应音色，选择第一个:', this.voiceOptions[0].value);
                  this.form.ttsVoiceId = this.voiceOptions[0].value;
                }
              } else {
                // 没有指定音色，选择第一个
                console.log('没有指定音色，选择第一个:', this.voiceOptions[0].value);
                this.form.ttsVoiceId = this.voiceOptions[0].value;
              }
            } else {
              // 设置默认音色
              this.voiceOptions = [
                { value: 'TTS_DoubaoTTS0001', label: '默认音色' }
              ];
              this.form.ttsVoiceId = 'TTS_DoubaoTTS0001';
            }
          }).catch(err => {
            console.error('获取音色列表失败', err);
            // 设置默认音色
            this.voiceOptions = [
              { value: 'TTS_DoubaoTTS0001', label: '默认音色' }
            ];
            this.form.ttsVoiceId = 'TTS_DoubaoTTS0001';
          });
        }
        
        uni.showToast({
          title: `"${template.agentName}"模板已应用`,
          icon: 'success'
        });
      } catch (error) {
        console.error('应用模板失败', error);
        uni.showToast({
          title: '应用模板失败',
          icon: 'none'
        });
      } finally {
        this.loadingTemplate = false;
      }
    },
    
    // 保存配置
    saveConfig() {
      if (!this.form.agentName) {
        uni.showToast({
          title: '请输入助手昵称',
          icon: 'none'
        });
        return;
      }
      
      this.isLoading = true;
      
      const configData = {
        agentName: this.form.agentName,
        ttsVoiceId: this.form.ttsVoiceId,
        chatHistoryConf: this.form.chatHistoryConf,
        systemPrompt: this.form.systemPrompt,
        summaryMemory: this.form.summaryMemory,
        langCode: this.form.langCode,
        language: this.form.language,
        ttsModelId: this.form.model.ttsModelId,
        vadModelId: this.form.model.vadModelId,
        asrModelId: this.form.model.asrModelId,
        llmModelId: this.form.model.llmModelId,
        memModelId: this.form.model.memModelId,
        intentModelId: this.form.model.intentModelId
      };
      
      console.log('保存配置:', configData);
      
      const savePromise = this.isEdit
        ? agentApi.updateAgent(this.id, configData)
        : agentApi.createAgent(configData);
        
      savePromise.then(res => {
        console.log('保存成功:', res);
        this.isLoading = false;
        uni.showToast({
          title: this.isEdit ? '更新成功' : '创建成功',
          icon: 'success',
          duration: 2000
        });
        
        setTimeout(() => {
          uni.navigateBack();
        }, 2000);
      }).catch(err => {
        console.error('保存配置失败', err);
        this.isLoading = false;
        uni.showToast({
          title: '保存失败',
          icon: 'none'
        });
      });
    },
    
    // 重置配置
    resetConfig() {
      uni.showModal({
        title: '提示',
        content: '确定要重置配置吗？',
        success: res => {
          if (res.confirm) {
            if (this.isEdit) {
              // 如果是编辑模式，重新获取原始配置
              this.fetchAgentConfig();
            } else {
              // 如果是新建模式，重置为空
              this.form = {
                agentName: '',
                ttsVoiceId: '',
                chatHistoryConf: 1,
                systemPrompt: '',
                summaryMemory: '',
                langCode: 'zh',
                language: '中文',
                sort: 0,
                model: {
                  ttsModelId: '',
                  vadModelId: '',
                  asrModelId: '',
                  llmModelId: '',
                  memModelId: '',
                  intentModelId: '',
                }
              };
              
              // 初始化默认值
              this.initDefaultValues();
            }
            
            uni.showToast({
              title: '已重置',
              icon: 'success'
            });
          }
        }
      });
    },
    // 切换音色类型
    switchVoiceType(type) {
      if (this.voiceType === type) return;
      
      this.voiceType = type;
      // 清空当前选择的音色ID和选项
      this.voiceOptions = [];
      this.form.ttsVoiceId = '';
      
      // 重新获取音色列表
      if (type === 'preset') {
        this.form.model.ttsModelId = 'TTS_HuoshanDoubleStreamTTS';
        this.fetchVoiceOptions(this.form.model.ttsModelId);
      } else {
        // 将表单数据的ttsModelId设置为空（克隆模式不需要模型ID）
        this.form.model.ttsModelId = 'TTS_FeiShuTTS';
        this.fetchVoiceOptions();
      }
    }
  }
}
</script>

<style lang="scss">
.config-page {
  min-height: 100vh;
  background-color: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.config-content {
  flex: 1;
  padding: 30rpx;
  position: relative;
  padding-bottom: calc(130rpx + env(safe-area-inset-bottom)); /* 为底部栏留出空间 */
}

.config-header {
  margin-bottom: 30rpx;
}

.config-hint {
  display: flex;
  align-items: center;
  background-color: #f0f9ff;
  padding: 20rpx;
  border-radius: 10rpx;
  
  .hint-text {
    font-size: 26rpx;
    color: #979db1;
    margin-left: 10rpx;
  }
}

.config-form {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 30rpx;
  // font-weight: bold;
  color: #3d4566;
  margin-bottom: 20rpx;
}

.template-section {
  margin-bottom: 30rpx;
}

.template-list {
  display: flex;
  flex-wrap: wrap;
  padding: 10rpx 0;
  margin-bottom: 10rpx;
}

.template-item {
  min-width: 150rpx;
  height: 60rpx;
  line-height: 60rpx;
  text-align: center;
  background-color: #e6ebff;
  color: #5778ff;
  font-size: 26rpx;
  border-radius: 30rpx;
  margin-right: 20rpx;
  margin-bottom: 20rpx;
  padding: 0 20rpx;
  white-space: nowrap;
  
  &-loading {
    opacity: 0.7;
  }
  
  &:active {
    background-color: #d0dbff; /* 点击状态颜色 */
  }
}

.model-section {
  margin-bottom: 30rpx;
}

.model-item {
  margin-bottom: 30rpx;
}

.model-label {
  font-size: 28rpx;
  color: #3d4566;
  margin-bottom: 20rpx;
  display: block;
}

.model-selector {
  width: 100%;
}

.picker-view {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80rpx;
  padding: 0 20rpx;
  background-color: #f5f6fa;
  border-radius: 8rpx;
}

.chat-history-options {
  margin-top: 20rpx;
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

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  padding: 20rpx;
  z-index: 100;
}

.bottom-bar-content {
  display: flex;
  gap: 20rpx;
  padding-bottom: env(safe-area-inset-bottom); /* 兼容iPhone底部安全区 */
  
  .u-button {
    flex: 1;
  }
}

.voice-type-switch {
  display: flex;
  margin-bottom: 20rpx;
}

.voice-type-item {
  padding: 8rpx 20rpx;
  font-size: 24rpx;
  border-radius: 30rpx;
  background-color: #f5f6fa;
  color: #666;
  margin-right: 20rpx;
  
  &.voice-type-active {
    background-color: #5778ff;
    color: #ffffff;
  }
}
</style>

