<template>
  <div class="auth-page-wrapper">
    <div class="register-container">
      <el-card class="register-card" shadow="always">
        <div class="register-header">
          <img alt="logo" src="@/assets/xiaozhi-logo.png" class="logo-img" />
          <h1 class="title">创建新账户</h1>
          <p class="subtitle">加入 飞鼠-ESP32，开启智能之旅</p>
        </div>

        <el-form class="register-form" @keyup.enter.native="register">
          <!-- 用户名注册 -->
          <template v-if="!enableMobileRegister">
            <el-form-item>
              <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="el-icon-user" size="large" />
            </el-form-item>
          </template>

          <!-- 手机号注册 -->
          <template v-if="enableMobileRegister">
            <el-form-item>
              <el-input v-model="form.mobile" placeholder="请输入手机号码" size="large">
                <el-select v-model="form.areaCode" slot="prepend" placeholder="区号">
                  <el-option v-for="item in mobileAreaList" :key="item.key" :label="item.key" :value="item.key" />
                </el-select>
              </el-input>
            </el-form-item>
            <el-form-item>
              <div class="captcha-wrapper">
                <el-input v-model="form.captcha" placeholder="请输入图形验证码" prefix-icon="el-icon-key" size="large" />
                <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" class="captcha-img" @click="fetchCaptcha" />
              </div>
            </el-form-item>
            <el-form-item>
              <div class="captcha-wrapper">
                <el-input v-model="form.mobileCaptcha" placeholder="请输入手机验证码" prefix-icon="el-icon-message"
                  size="large" />
                <el-button type="primary" class="send-captcha-btn" :disabled="!canSendMobileCaptcha"
                  @click="sendMobileCaptcha">
                  {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>
          </template>

          <el-form-item>
            <el-input v-model="form.password" placeholder="请输入密码" type="password" show-password
              prefix-icon="el-icon-lock" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.confirmPassword" placeholder="请确认密码" type="password" show-password
              prefix-icon="el-icon-lock" size="large" />
          </el-form-item>

          <!-- 用户名注册时的图形验证码 -->
          <el-form-item v-if="!enableMobileRegister">
            <div class="captcha-wrapper">
              <el-input v-model="form.captcha" placeholder="请输入验证码" prefix-icon="el-icon-key" size="large" />
              <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" class="captcha-img" @click="fetchCaptcha" />
            </div>
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <a class="link-text" @click="goToLogin">已有账号？立即登录</a>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="register-btn" @click="register" size="large">立即注册</el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <p class="agreement-text">
            注册即同意
            <a href="#" class="link-text">《用户协议》</a>
            和
            <a href="#" class="link-text">《隐私政策》</a>
          </p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import Api from '@/apis/api';
import VersionFooter from '@/components/VersionFooter.vue';
import { getUUID, goToPage, showDanger, showSuccess, validateMobile } from '@/utils';
import { mapState } from 'vuex';

export default {
  name: 'register',
  components: {
    VersionFooter
  },
  computed: {
    ...mapState({
      allowUserRegister: state => state.pubConfig.allowUserRegister,
      enableMobileRegister: state => state.pubConfig.enableMobileRegister,
      mobileAreaList: state => state.pubConfig.mobileAreaList
    }),
    canSendMobileCaptcha() {
      return this.countdown === 0 && validateMobile(this.form.mobile, this.form.areaCode);
    }
  },
  data() {
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        captcha: '',
        captchaId: '',
        areaCode: '+86',
        mobile: '',
        mobileCaptcha: ''
      },
      captchaUrl: '',
      countdown: 0,
      timer: null
    }
  },
  mounted() {
    this.$store.dispatch('fetchPubConfig').then(() => {
      if (!this.allowUserRegister) {
        showDanger('当前不允许普通用户注册');
        setTimeout(() => {
          goToPage('/login');
        }, 1500);
      }
    });
    this.fetchCaptcha();
  },
  methods: {
    // 复用验证码获取方法
    fetchCaptcha() {
      this.form.captchaId = getUUID();
      Api.user.getCaptcha(this.form.captchaId, (res) => {
        if (res.status === 200) {
          const blob = new Blob([res.data], {
            type: res.data.type
          });
          this.captchaUrl = URL.createObjectURL(blob);

        } else {
          showDanger('验证码加载失败，点击刷新');
        }
      });
    },

    // 封装输入验证逻辑
    validateInput(input, message) {
      if (!input.trim()) {
        showDanger(message);
        return false;
      }
      return true;
    },

    // 发送手机验证码
    sendMobileCaptcha() {
      if (!validateMobile(this.form.mobile, this.form.areaCode)) {
        showDanger('请输入正确的手机号码');
        return;
      }

      // 验证图形验证码
      if (!this.validateInput(this.form.captcha, '请输入图形验证码')) {
        this.fetchCaptcha();
        return;
      }

      // 清除可能存在的旧定时器
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }

      // 开始倒计时
      this.countdown = 60;
      this.timer = setInterval(() => {
        if (this.countdown > 0) {
          this.countdown--;
        } else {
          clearInterval(this.timer);
          this.timer = null;
        }
      }, 1000);

      // 调用发送验证码接口
      Api.user.sendSmsVerification({
        phone: this.form.areaCode + this.form.mobile,
        captcha: this.form.captcha,
        captchaId: this.form.captchaId
      }, (res) => {
        showSuccess('验证码发送成功');
      }, (err) => {
        showDanger(err.data.msg || '验证码发送失败');
        this.countdown = 0;
        if (this.timer) {
          clearInterval(this.timer);
          this.timer = null;
        }
        this.fetchCaptcha();
      });
    },

    // 注册逻辑
    register() {
      if (this.enableMobileRegister) {
        // 手机号注册验证
        if (!validateMobile(this.form.mobile, this.form.areaCode)) {
          showDanger('请输入正确的手机号码');
          return;
        }
        if (!this.form.mobileCaptcha) {
          showDanger('请输入手机验证码');
          return;
        }
        this.form.username = this.form.areaCode + this.form.mobile;
      } else {
        // 用户名注册验证
        if (!this.validateInput(this.form.username, '用户名不能为空')) {
          return;
        }
        // 图形验证码
        if (!this.validateInput(this.form.captcha, '请输入验证码')) {
          return;
        }
      }

      if (!this.validateInput(this.form.password, '密码不能为空')) {
        return;
      }

      if (this.form.password !== this.form.confirmPassword) {
        showDanger('两次输入的密码不一致');
        return;
      }

      Api.user.register(this.form, ({
        data
      }) => {
        showSuccess(data.msg);
        setTimeout(() => {
          goToPage('/login');
        }, 1500);
      }, (err) => {
        showDanger(err.data.msg || '注册失败');
        this.fetchCaptcha();
      })
    },
    goToLogin() {
      goToPage('/login')
    }
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.register-container {
  width: 450px;
  margin: 20px;
}

.register-card {
  border-radius: 12px;
  border: 1px solid mix(white, $--color-primary, 90%);
  padding: 20px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  .logo-img {
    width: 60px;
    height: 60px;
    margin-bottom: 15px;
  }

  .title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 5px 0;
  }

  .subtitle {
    font-size: 14px;
    color: #606266;
    margin: 0;
  }
}

.register-form {
  ::v-deep .el-input__inner {
    border-radius: 8px;

    &:focus {
      border-color: $--color-primary;
    }
  }

  ::v-deep .el-select .el-input {
    width: 110px;
  }

  ::v-deep .el-input-group__prepend {
    border-top-left-radius: 8px;
    border-bottom-left-radius: 8px;
    border-color: #DCDFE6;
  }
}

::v-deep .el-select-dropdown .el-select-dropdown__item.selected {
  color: $--color-primary;
  background-color: mix(white, $--color-primary, 90%);
}

::v-deep .el-select-dropdown__item.hover,
::v-deep .el-select-dropdown__item:hover {
  background-color: mix(white, $--color-primary, 95%);
}

.captcha-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;

  .captcha-img {
    height: 40px;
    cursor: pointer;
    border-radius: 8px;
  }
}

.send-captcha-btn {
  white-space: nowrap;
  background-color: $--color-primary;
  border-color: $--color-primary;

  &:hover,
  &:focus {
    background-color: lighten($--color-primary, 5%);
    border-color: lighten($--color-primary, 5%);
    color: white;
  }

  &.is-disabled {
    background-color: mix(white, $--color-primary, 50%);
    border-color: mix(white, $--color-primary, 50%);
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.link-text {
  color: $--color-primary;
  cursor: pointer;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

.register-btn {
  width: 100%;
  border-radius: 8px;
  background-color: $--color-primary;
  border-color: $--color-primary;
  font-size: 16px;
  font-weight: 500;

  &:hover,
  &:focus {
    background-color: lighten($--color-primary, 5%);
    border-color: lighten($--color-primary, 5%);
    color: white;
  }
}

.register-footer {
  margin-top: 20px;
  text-align: center;
  color: #909399;
}

.agreement-text {
  font-size: 12px;
}
</style>
