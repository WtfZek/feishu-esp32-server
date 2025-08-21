<template>
  <div class="auth-page-wrapper">
    <div class="retrieve-container">
      <el-card class="retrieve-card" shadow="always">
        <div class="retrieve-header">
          <h1 class="title">重置密码</h1>
          <p class="subtitle">我们将通过短信验证码来帮助您重置密码</p>
        </div>

        <el-form class="retrieve-form" @keyup.enter.native="retrievePassword">
          <el-form-item>
            <el-input v-model="form.mobile" placeholder="请输入注册时使用的手机号码" size="large">
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
          <el-form-item>
            <el-input v-model="form.newPassword" placeholder="请输入新密码" type="password" show-password
              prefix-icon="el-icon-lock" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.confirmPassword" placeholder="请确认新密码" type="password" show-password
              prefix-icon="el-icon-lock" size="large" />
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <a class="link-text" @click="goToLogin">返回登录</a>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="retrieve-btn" @click="retrievePassword" size="large">立即修改</el-button>
          </el-form-item>
        </el-form>
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
  name: 'retrievePassword',
  components: {
    VersionFooter
  },
  computed: {
    ...mapState({
      mobileAreaList: state => state.pubConfig.mobileAreaList
    }),
    canSendMobileCaptcha() {
      return this.countdown === 0 && validateMobile(this.form.mobile, this.form.areaCode);
    }
  },
  data() {
    return {
      form: {
        areaCode: '+86',
        mobile: '',
        captcha: '',
        captchaId: '',
        mobileCaptcha: '', //
        newPassword: '',
        confirmPassword: ''
      },
      captchaUrl: '',
      countdown: 0,
      timer: null
    }
  },
  mounted() {
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

    // 修改逻辑
    retrievePassword() {
      // 验证逻辑
      if (!validateMobile(this.form.mobile, this.form.areaCode)) {
        showDanger('请输入正确的手机号码');
        return;
      }
      if (!this.validateInput(this.form.captcha, '请输入图形验证码')) {
        return;
      }
      if (!this.validateInput(this.form.mobileCaptcha, '请输入短信验证码')) {
        return;
      }
      if (!this.validateInput(this.form.newPassword, '请输入新密码')) {
        return;
      }
      if (this.form.newPassword !== this.form.confirmPassword) {
        showDanger('两次输入的密码不一致');
        return;
      }

      Api.user.retrievePassword({
        phone: this.form.areaCode + this.form.mobile,
        password: this.form.newPassword,
        code: this.form.mobileCaptcha
      }, (res) => {
        showSuccess('密码重置成功');
        setTimeout(() => {
          goToPage('/login');
        }, 1500);
      }, (err) => {
        showDanger(err.data.msg || '重置失败');
        this.fetchCaptcha();
      });
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

.retrieve-container {
  width: 450px;
  margin: 20px;
}

.retrieve-card {
  border-radius: 12px;
  border: 1px solid mix(white, $--color-primary, 90%);
  padding: 20px;
}

.retrieve-header {
  text-align: center;
  margin-bottom: 30px;

  .title {
    font-size: 24px;
    font-weight: 600;
    color: #37474F;
    margin: 0 0 5px 0;
  }

  .subtitle {
    font-size: 14px;
    color: #78909C;
    margin: 0;
  }
}

.retrieve-form {
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

.retrieve-btn {
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
</style>
