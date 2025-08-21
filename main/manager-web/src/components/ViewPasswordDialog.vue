<template>
  <el-dialog :visible.sync="visible" width="400px" center>
    <div class="dialog-title">
      <div class="icon-wrapper">
        <img src="@/assets/login/shield.png" alt="shield" class="shield-icon" />
      </div>
      用户新密码
    </div>
    <div class="divider" />
    <div class="content-wrapper">
      <div class="password-label">
        <span style="color: red;">*</span>
        用户新密码：
      </div>
      <div class="input-container">
        <el-input v-model="password" type="text" :readonly="true" class="password-input" />
      </div>
    </div>
    <div class="button-group">
      <div class="dialog-btn btn-close" @click="closeDialog">
        关闭
      </div>
      <div class="dialog-btn btn-copy" @click="copyPassword">
        复制密码
      </div>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'ViewPasswordDialog',
  props: {
    visible: { type: Boolean, required: true },
    password: { type: String, default: '' }
  },
  methods: {
    closeDialog() {
      this.$emit('update:visible', false)
    },
    copyPassword() {
      navigator.clipboard.writeText(this.password)
      this.$message.success({
        message: '密码已复制',
        showClose: true
      })
    }
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

.shield-icon {
  width: 19px;
  height: 23px;
  filter: brightness(0) invert(1);
}

.divider {
  height: 1px;
  background: #e8f0ff;
}

.content-wrapper {
  margin: 22px 15px;
}

.password-label {
  font-weight: 400;
  font-size: 14px;
  text-align: left;
  color: #3d4566;
}

.input-container {
  margin-top: 12px;
  border: 1px solid #e4e6ef;
  background: #f6f8fb;
  border-radius: 15px;
  overflow: hidden;
}

.password-input {
  font-weight: bold;
  color: #333;
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

.btn-close {
  background: $--color-primary-light;
  border: 1px solid $--color-primary;
  color: $--color-primary;

  &:hover {
    background: mix($--color-primary, #ffffff, 25%);
  }
}

.btn-copy {
  background: $--color-primary;
  color: white;

  &:hover {
    background: darken($--color-primary, 5%);
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

::v-deep .el-input__inner {
  background-color: #f6f8fb !important;
  cursor: default !important;
  border: none;
}
</style>
