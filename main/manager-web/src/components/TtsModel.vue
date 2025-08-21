<template>
  <el-dialog :visible.sync="localVisible" width="90%" @close="handleClose" :show-close="false" :append-to-body="true"
    :close-on-click-modal="true">
    <button class="custom-close-btn" @click="handleClose">
      ×
    </button>
    <div class="scroll-wrapper">
      <div class="table-container" ref="tableContainer" @scroll="handleScroll">
        <el-table v-loading="loading" :data="filteredTtsModels" style="width: 100%;" class="data-table"
          header-row-class-name="table-header" :fit="true" element-loading-text="拼命加载中"
          element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.8)">
          <el-table-column label="选择" width="50" align="center">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.selected"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="音色编码" align="center">
            <template slot-scope="scope">
              <el-input v-if="scope.row.editing" v-model="scope.row.voiceCode"></el-input>
              <span v-else>{{ scope.row.voiceCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="音色名称" align="center">
            <template slot-scope="scope">
              <el-input v-if="scope.row.editing" v-model="scope.row.voiceName"></el-input>
              <span v-else>{{ scope.row.voiceName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="语言类型" align="center">
            <template slot-scope="scope">
              <el-input v-if="scope.row.editing" v-model="scope.row.languageType"></el-input>
              <span v-else>{{ scope.row.languageType }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="!showReferenceColumns" label="试听" align="center" class-name="audio-column">
            <template slot-scope="scope">
              <div class="custom-audio-container">
                <el-input v-if="scope.row.editing" v-model="scope.row.voiceDemo" placeholder="请输入MP3地址"
                  class="audio-input">
                </el-input>
                <AudioPlayer v-else-if="isValidAudioUrl(scope.row.voiceDemo)" :audioUrl="scope.row.voiceDemo" />
              </div>
            </template>
          </el-table-column>
          <el-table-column v-if="!showReferenceColumns" label="备注" align="center">
            <template slot-scope="scope">
              <el-input v-if="scope.row.editing" type="textarea" :rows="1" autosize v-model="scope.row.remark"
                placeholder="这里是备注" class="remark-input"></el-input>
              <span v-else>{{ scope.row.remark }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="showReferenceColumns" label="克隆音频路径" align="center">
            <template slot-scope="scope">
              <el-input v-if="scope.row.editing" v-model="scope.row.referenceAudio" placeholder="这里是克隆音频路径"></el-input>
              <span v-else>{{ scope.row.referenceAudio }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="showReferenceColumns" label="克隆音频文本" align="center">
            <template slot-scope="scope">
              <el-input v-if="scope.row.editing" v-model="scope.row.referenceText" placeholder="这里是克隆音频对应文本"></el-input>
              <span v-else>{{ scope.row.referenceText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="150">
            <template slot-scope="scope">
              <template v-if="!scope.row.editing">
                <el-button type="text" size="mini" @click="startEdit(scope.row)" class="edit-btn">
                  编辑
                </el-button>
                <el-button type="text" size="mini" @click="deleteRow(scope.row)" class="delete-btn">
                  删除
                </el-button>
              </template>
              <el-button v-else type="success" size="mini" @click="saveEdit(scope.row)" class="save-Tts">保存
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 自定义滚动条 -->
      <div class="custom-scrollbar" ref="scrollbar">
        <div class="custom-scrollbar-track" ref="scrollbarTrack" @click="handleTrackClick">
          <div class="custom-scrollbar-thumb" ref="scrollbarThumb" @mousedown="startDrag"></div>
        </div>
      </div>
    </div>
    <div class="action-buttons">
      <el-button type="primary" size="mini" @click="toggleSelectAll">
        {{ selectAll ? '取消全选' : '全选' }}
      </el-button>
      <el-button type="primary" size="mini" @click="addNew">
        新增
      </el-button>
      <el-button type="primary" size="mini" @click="deleteRow(filteredTtsModels.filter(row => row.selected))">
        删除
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import Api from "@/apis/api";
import AudioPlayer from './AudioPlayer.vue';

export default {
  components: { AudioPlayer },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    ttsModelId: {
      type: String,
      required: true
    },
    modelConfig: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      localVisible: this.visible,
      searchQuery: '',
      editDialogVisible: false,
      editVoiceData: {},
      ttsModels: [],
      currentPage: 1,
      pageSize: 10000,
      total: 0,
      isDragging: false,
      startY: 0,
      scrollTop: 0,
      selectAll: false,
      selectedRows: [],
      loading: false,
      showReferenceColumns: false, // 控制是否显示参考列
    };
  },
  watch: {
    visible(newVal) {
      this.localVisible = newVal;
      if (newVal) {
        this.currentPage = 1;
        this.updateShowReferenceColumns(); // 更新显示状态
        this.loadData(); // 对话框显示时加载数据
        this.$nextTick(() => {
          this.updateScrollbar();
        });
      }
    },
    modelConfig: {
      handler(newVal) {
        this.updateShowReferenceColumns();
      },
      immediate: true
    },
    filteredTtsModels() {
      this.$nextTick(() => {
        this.updateScrollbar();
      });
    }
  },
  computed: {
    filteredTtsModels() {
      return this.ttsModels.filter(model =>
        model.voiceName.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    }
  },
  mounted() {
    this.updateScrollbar();
    window.addEventListener('resize', this.updateScrollbar);
    window.addEventListener('mouseup', this.stopDrag);
    window.addEventListener('mousemove', this.handleDrag);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateScrollbar);
    window.removeEventListener('mouseup', this.stopDrag);
    window.removeEventListener('mousemove', this.handleDrag);
  },
  methods: {
    // 更新是否显示参考列
    updateShowReferenceColumns() {
      if (this.modelConfig && this.modelConfig.configJson) {
        const providerType = this.modelConfig.configJson.type;
        this.showReferenceColumns = ['fishspeech', 'gpt_sovits_v2', 'gpt_sovits_v3'].includes(providerType);
      } else {
        this.showReferenceColumns = false;
      }
    },

    loadData() {
      this.loading = true;
      const params = {
        ttsModelId: this.ttsModelId,
        page: this.currentPage,
        limit: this.pageSize,
        name: this.searchQuery
      };
      Api.timbre.getVoiceList(params, (data) => {
        if (data.code === 0) {
          this.ttsModels = data.data.list
            .map(item => ({
              id: item.id || '',
              voiceCode: item.ttsVoice || '',
              voiceName: item.name || '未命名音色',
              languageType: item.languages || '',
              remark: item.remark || '',
              referenceAudio: item.referenceAudio || '',
              referenceText: item.referenceText || '',
              voiceDemo: item.voiceDemo || '',
              selected: false,
              editing: false,
              sort: Number(item.sort)
            }))
            .sort((a, b) => a.sort - b.sort);
          this.total = data.total;
        } else {
          this.$message.error({
            message: data.msg || '获取音色列表失败',
            showClose: true
          });
        }
        this.loading = false;
      }, (err) => {
        console.error('加载失败:', err);
        this.$message.error({
          message: '加载音色数据失败',
          showClose: true
        });
        this.loading = false;
      });
    },

    handleClose() {
      // 重置状态
      this.ttsModels = [];
      this.currentPage = 1;
      this.total = 0;
      this.selectAll = false;
      this.searchQuery = '';
      this.showReferenceColumns = false;

      this.localVisible = false;
      this.$emit('update:visible', false);
    },

    updateScrollbar() {
      const container = this.$refs.tableContainer;
      const scrollbarThumb = this.$refs.scrollbarThumb;
      const scrollbarTrack = this.$refs.scrollbarTrack;

      if (!container || !scrollbarThumb || !scrollbarTrack) return;

      const { scrollHeight, clientHeight } = container;
      const trackHeight = scrollbarTrack.clientHeight;
      const thumbHeight = Math.max((clientHeight / scrollHeight) * trackHeight, 20);

      scrollbarThumb.style.height = `${thumbHeight}px`;
      this.updateThumbPosition();
    },

    updateThumbPosition() {
      const container = this.$refs.tableContainer;
      const scrollbarThumb = this.$refs.scrollbarThumb;
      const scrollbarTrack = this.$refs.scrollbarTrack;

      if (!container || !scrollbarThumb || !scrollbarTrack) return;

      const { scrollHeight, clientHeight, scrollTop } = container;
      const trackHeight = scrollbarTrack.clientHeight;
      const thumbHeight = scrollbarThumb.clientHeight;
      const maxTop = trackHeight - thumbHeight;
      const thumbTop = (scrollTop / (scrollHeight - clientHeight)) * (trackHeight - thumbHeight);

      scrollbarThumb.style.top = `${Math.min(thumbTop, maxTop)}px`;
    },

    handleScroll() {
      const container = this.$refs.tableContainer;
      if (container.scrollTop + container.clientHeight >= container.scrollHeight - 50) {
        if (this.currentPage * this.pageSize < this.total) {
          this.currentPage++;
          this.loadData();
        }
      }
      this.updateThumbPosition();
    },

    startDrag(e) {
      this.isDragging = true;
      this.startY = e.clientY;
      this.scrollTop = this.$refs.tableContainer.scrollTop;
      e.preventDefault();
    },

    stopDrag() {
      this.isDragging = false;
    },

    handleDrag(e) {
      if (!this.isDragging) return;

      const container = this.$refs.tableContainer;
      const scrollbarTrack = this.$refs.scrollbarTrack;
      const scrollbarThumb = this.$refs.scrollbarThumb;
      const deltaY = e.clientY - this.startY;
      const trackHeight = scrollbarTrack.clientHeight;
      const thumbHeight = scrollbarThumb.clientHeight;
      const maxScrollTop = container.scrollHeight - container.clientHeight;

      const scrollRatio = (trackHeight - thumbHeight) / maxScrollTop;
      container.scrollTop = this.scrollTop + deltaY / scrollRatio;
    },

    handleTrackClick(e) {
      const container = this.$refs.tableContainer;
      const scrollbarTrack = this.$refs.scrollbarTrack;
      const scrollbarThumb = this.$refs.scrollbarThumb;

      if (!container || !scrollbarTrack || !scrollbarThumb) return;

      const trackRect = scrollbarTrack.getBoundingClientRect();
      const thumbHeight = scrollbarThumb.clientHeight;
      const clickPosition = e.clientY - trackRect.top;
      const thumbCenter = clickPosition - thumbHeight / 2;

      const trackHeight = scrollbarTrack.clientHeight;
      const maxTop = trackHeight - thumbHeight;
      const newTop = Math.max(0, Math.min(thumbCenter, maxTop));

      scrollbarThumb.style.top = `${newTop}px`;
      container.scrollTop = (newTop / (trackHeight - thumbHeight)) * (container.scrollHeight - container.clientHeight);
    },

    startEdit(row) {
      row.editing = true;
      this.$set(row, 'originalData', { ...row });
    },

    saveEdit(row) {
      if (!row.voiceCode || !row.voiceName || !row.languageType) {
        this.$message.error({
          message: '音色编码、音色名称和语言类型不能为空',
          showClose: true
        });
        return;
      }

      try {
        const params = {
          id: row.id,
          voiceCode: row.voiceCode,
          voiceName: row.voiceName,
          languageType: row.languageType,
          remark: row.remark,
          ttsModelId: this.ttsModelId,
          voiceDemo: row.voiceDemo || '',
          sort: row.sort
        };

        // 只有在显示参考列的情况下才添加参考字段
        if (this.showReferenceColumns) {
          params.referenceAudio = row.referenceAudio;
          params.referenceText = row.referenceText;
        }

        let res;
        if (row.id) {
          // 已有ID，执行更新操作
          Api.timbre.updateVoice(params, (response) => {
            res = response;
            this.handleResponse(res, row);
          });
        } else {
          // 没有ID，执行新增操作
          Api.timbre.saveVoice(params, (response) => {
            res = response;
            this.handleResponse(res, row);
          });
        }
      } catch (error) {
        console.error('操作失败:', error);
        // 异常情况下也恢复原始数据
        if (row.originalData) {
          Object.assign(row, row.originalData);
          row.editing = false;
          delete row.originalData;
        }
        this.$message.error({
          message: '操作失败，请重试',
          showClose: true
        });
      }
    },

    handleResponse(res, row) {
      if (res.code === 0) {
        this.$message.success({
          message: row.id ? '修改成功' : '保存成功',
          showClose: true
        });
        row.editing = false;
        delete row.originalData;
        this.loadData(); // 刷新数据
      } else {
        // 保存失败时恢复原始数据
        if (row.originalData) {
          Object.assign(row, row.originalData);
          row.editing = false;
          delete row.originalData;
        }
        this.$message.error({
          message: res.msg || (row.id ? '修改失败' : '保存失败'),
          showClose: true
        });
      }
    },

    toggleSelectAll() {
      this.selectAll = !this.selectAll;
      this.filteredTtsModels.forEach(row => {
        row.selected = this.selectAll;
      });
    },

    addNew() {
      const hasEditing = this.ttsModels.some(row => row.editing);
      if (hasEditing) {
        this.$message.warning('请先完成当前编辑再新增');
        return;
      }

      const maxSort = this.ttsModels.length > 0
        ? Math.max(...this.ttsModels.map(item => Number(item.sort) || 0))
        : 0;

      const newRow = {
        voiceCode: '',
        voiceName: '',
        languageType: '中文',
        voiceDemo: '',
        remark: '',
        referenceAudio: '',
        referenceText: '',
        selected: false,
        editing: true,
        sort: maxSort + 1
      };

      this.ttsModels.unshift(newRow);
    },

    deleteRow(row) {
      // 处理单个音色或音色数组
      const voices = Array.isArray(row) ? row : [row];

      if (Array.isArray(row) && row.length === 0) {
        this.$message.warning("请先选择需要删除的音色");
        return;
      }


      const voiceCount = voices.length;
      this.$confirm(`确定要删除选中的${voiceCount}个音色吗？`, "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        distinguishCancelAndClose: true
      }).then(() => {
        const ids = voices.map(voice => voice.id);
        if (ids.some(id => !id)) {
          this.$message.error("存在无效的音色ID");
          return;
        }

        Api.timbre.deleteVoice(ids, ({ data }) => {
          if (data.code === 0) {
            this.$message.success({
              message: `成功删除${voiceCount}个参数`,
              showClose: true
            });
            this.loadData(); // 刷新参数列表
          } else {
            this.$message.error({
              message: data.msg || '删除失败，请重试',
              showClose: true
            });
          }
        });
      }).catch(action => {
        if (action === 'cancel') {
          this.$message({
            type: 'info',
            message: '已取消删除操作',
            duration: 1000
          });
        } else {
          this.$message({
            type: 'info',
            message: '操作已关闭',
            duration: 1000
          });
        }
      });
    },

    isValidAudioUrl(url) {
      return url && (url.endsWith('.mp3') || url.endsWith('.ogg') || url.endsWith('.wav'));
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

::v-deep .el-dialog {
  background-color: transparent !important;
  box-shadow: none !important;
  overflow: hidden;
}

::v-deep .el-dialog__header {
  display: none;
}

::v-deep .el-dialog__body {
  padding: 0;
  border-radius: 20px;
  background-color: #f7faff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.custom-close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 2px solid #cfcfcf;
  background: none;
  font-size: 30px;
  font-weight: lighter;
  color: #cfcfcf;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  padding: 0;
  outline: none;
  transition: all 0.3s;
}

.custom-close-btn:hover {
  color: $--color-primary;
  border-color: $--color-primary;
}

.scroll-wrapper {
  position: relative;
  overflow: hidden;
  height: calc(80vh - 60px);
}

.table-container {
  overflow-y: auto;
  height: 100%;
}

.data-table {
  background-color: transparent;
}

.table-header th {
  background-color: #f0f4ff !important;
  color: #333;
}

.custom-audio-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-buttons {
  padding: 10px 20px;
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  background-color: #f7faff;
  border-top: 1px solid #EBEEF5;
}

.action-buttons .el-button {
  border: none;
  border-radius: 4px;
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.action-buttons .el-button:nth-child(1) {
  background: $--color-primary;
}

.action-buttons .el-button:nth-child(2) {
  background: $--color-success;
}

.action-buttons .el-button:nth-child(3) {
  background: $--color-danger;
}

.edit-btn,
.delete-btn {
  color: $--color-primary !important;
  font-weight: 500;
}

.save-Tts {
  background-color: $--color-success;
  color: white;
  border-radius: 4px;
  padding: 5px 10px;
}

::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: $--color-primary;
  border-color: $--color-primary;
}
</style>