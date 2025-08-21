<template>
  <form>
    <el-dialog :visible.sync="dialogVisible" width="24%" center>
      <div class="dialog-title">
        <div class="icon-wrapper">
          <img loading="lazy" src="@/assets/login/shield.png" alt="shield" class="shield-icon" />
        </div>
        修改密码
      </div>
      <div class="divider" />
      <div class="form-content">
        <div class="form-group">
          <label class="form-label"><span class="required-star">*</span>旧密码：</label>
          <div class="input-container">
            <el-input placeholder="请输入旧密码" v-model="oldPassword" type="password" show-password />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label"><span class="required-star">*</span>新密码：</label>
          <div class="input-container">
            <el-input placeholder="请输入新密码" v-model="newPassword" type="password" show-password />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label"><span class="required-star">*</span>确认新密码：</label>
          <div class="input-container">
            <el-input placeholder="请再次输入新密码" v-model="confirmNewPassword" type="password" show-password />
          </div>
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
  </form>
</template>

<script>
import userApi from '@/apis/module/user';
import { mapActions } from 'vuex';

export default {
  name: 'ChangePasswordDialog',
  props: {
    value: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      dialogVisible: this.value,
      oldPassword: "",
      newPassword: "",
      confirmNewPassword: ""
    }
  },
  watch: {
    value(val) {
      this.dialogVisible = val;
    },
    dialogVisible(val) {
      this.$emit('input', val);
    }
  },
  methods: {
    ...mapActions(['logout']), // 引入Vuex的logout action
    confirm() {
      if (!this.oldPassword.trim() || !this.newPassword.trim() || !this.confirmNewPassword.trim()) {
        this.$message.error('请填写所有字段');
        return;
      }
      if (this.newPassword !== this.confirmNewPassword) {
        this.$message.error('两次输入的新密码不一致');
        return;
      }
      if (this.newPassword === this.oldPassword) {
        this.$message.error('新密码不能与旧密码相同');
        return;
      }

      // 修改后的接口调用
      userApi.changePassword(this.oldPassword, this.newPassword, (res) => {
        if (res.data.code === 0) {
          this.$message.success({
            message: '密码修改成功，请重新登录',
            showClose: true
          });
          this.logout().then(() => {
            this.$router.push('/login');
            this.$emit('update:visible', false);
          });
        } else {
          this.$message.error(res.data.msg || '密码修改失败');
        }
      }, (err) => {
        this.$message.error(err.msg || '密码修改失败');
      });
      this.$emit('input', false);
    },
    cancel() {
      this.dialogVisible = false;
      this.resetForm();
    },
    resetForm() {
      this.oldPassword = "";
      this.newPassword = "";
      this.confirmNewPassword = "";
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

.form-content {
  margin: 22px 15px;
}

.form-group {
  margin-bottom: 12px;
}

.form-label {
  font-weight: 400;
  font-size: 14px;
  text-align: left;
  color: #3d4566;
  display: block;
  margin-bottom: 12px;
}

.required-star {
  color: red;
  display: inline-block;
}

.input-container {
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
  background-color: transparent !important;
  border: none;
}
</style>