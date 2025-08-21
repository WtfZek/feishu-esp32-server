<template>
  <el-dialog :visible="visible" @close="handleClose" width="25%" center @open="handleOpen">
    <div class="dialog-title">
      <div class="icon-wrapper">
        <img loading="lazy" src="@/assets/home/equipment.png" alt="equipment" class="icon" />
      </div>
      添加智能体
    </div>
    <div class="divider" />
    <div class="content-wrapper">
      <div class="form-label">
        <span class="required-star">*</span> 智能体名称：
      </div>
      <div class="input-container">
        <el-input ref="inputRef" placeholder="请输入智能体名称.." v-model="wisdomBodyName" @keyup.enter.native="confirm" />
      </div>
    </div>
    <div class="button-group">
      <div class="dialog-btn btn-confirm" @click="confirm">
        确定
      </div>
      <div class="dialog-btn btn-cancel" @click="cancel">
        取消
      </div>
    </div>
  </el-dialog>
</template>

<script>
import Api from '@/apis/api';

export default {
  name: 'AddWisdomBodyDialog',
  props: {
    visible: { type: Boolean, required: true }
  },
  data() {
    return {
      wisdomBodyName: "",
      inputRef: null
    }
  },
  methods: {
    handleOpen() {
      this.$nextTick(() => {
        this.$refs.inputRef.focus();
      });
    },
    confirm() {
      if (!this.wisdomBodyName.trim()) {
        this.$message.error('请输入智能体名称');
        return;
      }
      Api.agent.addAgent(this.wisdomBodyName, (res) => {
        this.$message.success({
          message: '添加成功',
          showClose: true
        });
        this.$emit('confirm', res);
        this.$emit('update:visible', false);
        this.wisdomBodyName = "";
      });
    },
    cancel() {
      this.$emit('update:visible', false)
      this.wisdomBodyName = ""
    },
    handleClose() {
      this.$emit('update:visible', false);
    },
  }
}
</script>

<style scoped lang="scss">
@import '~@/styles/variables.scss';

.dialog-title {
  margin: 0 10px 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  font-size: 20px;
  text-align: left;
  color: #3d4566;
}

.icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: $--color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  width: 18px;
  height: 15px;
}

.divider {
  height: 1px;
  background: #e8f0ff;
}

.content-wrapper {
  margin: 22px 15px;
}

.form-label {
  font-weight: 400;
  text-align: left;
  color: #3d4566;
}

.required-star {
  color: red;
  display: inline-block;
}

.input-container {
  margin-top: 12px;
  border: 1px solid #e4e6ef;
  background: #f6f8fb;
  border-radius: 15px;
  overflow: hidden;
}

.button-group {
  display: flex;
  margin: 15px;
  gap: 7px;
}

.dialog-btn {
  cursor: pointer;
  flex: 1;
  border-radius: 23px;
  height: 40px;
  font-weight: 500;
  font-size: 12px;
  line-height: 40px;
  text-align: center;
  transition: all 0.3s;
}

.btn-confirm {
  background: $--color-primary;
  color: white;
  &:hover {
    background: darken($--color-primary, 5%);
  }
}

.btn-cancel {
  background: $--color-primary-light;
  border: 1px solid $--color-primary;
  color: $--color-primary;
  &:hover {
    background: mix($--color-primary, #ffffff, 25%);
  }
}

::v-deep .el-dialog {
  border-radius: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

::v-deep .el-dialog__headerbtn {
  display: none;
}

::v-deep .el-dialog__body {
  padding: 4px 6px;
}

::v-deep .el-dialog__header {
  padding: 10px;
}

::v-deep .el-input__inner {
  border: none;
  background: transparent;
}
</style>