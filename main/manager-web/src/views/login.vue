<template>
  <div class="auth-page-wrapper">
    <div class="login-container">
      <el-card class="login-card" shadow="always">
        <div class="login-header">
          <img alt="logo" src="@/assets/xiaozhi-logo.png" class="logo-img" />
          <h1 class="title">欢迎回来</h1>
          <p class="subtitle">登录以继续使用 飞鼠-ESP32</p>
        </div>

        <el-form class="login-form" @keyup.enter.native="login">
          <!-- 用户名登录 -->
          <template v-if="!isMobileLogin">
            <el-form-item>
              <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="el-icon-user" size="large" />
            </el-form-item>
          </template>

          <!-- 手机号登录 -->
          <template v-else>
             <el-form-item>
                <el-input v-model="form.mobile" placeholder="请输入手机号码" size="large">
                   <el-select v-model="form.areaCode" slot="prepend" placeholder="区号" style="width: 100px;">
                    <el-option v-for="item in mobileAreaList" :key="item.key" :label="item.key" :value="item.key" />
                  </el-select>
                </el-input>
              </el-form-item>
          </template>

          <el-form-item>
            <el-input v-model="form.password" placeholder="请输入密码" type="password" show-password prefix-icon="el-icon-lock" size="large" />
          </el-form-item>

          <el-form-item>
            <div class="captcha-wrapper">
              <el-input v-model="form.captcha" placeholder="请输入验证码" prefix-icon="el-icon-key" size="large" />
              <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" class="captcha-img" @click="fetchCaptcha" />
            </div>
          </el-form-item>

          <el-form-item>
             <div class="form-actions">
                <a class="link-text" @click="goToRegister" v-if="allowUserRegister">新用户注册</a>
                <a class="link-text" @click="goToForgetPassword" v-if="enableMobileRegister">忘记密码?</a>
             </div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="login" size="large">登 录</el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <div class="login-type-switcher" v-if="enableMobileRegister">
            <el-tooltip content="用户名登录" placement="bottom">
              <div class="switcher-icon" :class="{'active': !isMobileLogin}" @click="switchLoginType('username')">
                <i class="el-icon-user"></i>
              </div>
            </el-tooltip>
             <el-tooltip content="手机号码登录" placement="bottom">
              <div class="switcher-icon" :class="{'active': isMobileLogin}" @click="switchLoginType('mobile')">
                 <i class="el-icon-mobile"></i>
              </div>
            </el-tooltip>
          </div>
          <p class="agreement-text">
            登录即同意
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
  name: 'login',
  components: {
    VersionFooter
  },
  computed: {
    ...mapState({
      allowUserRegister: state => state.pubConfig.allowUserRegister,
      enableMobileRegister: state => state.pubConfig.enableMobileRegister,
      mobileAreaList: state => state.pubConfig.mobileAreaList
    })
  },
  data() {
    return {
      activeName: "username",
      form: {
        username: '',
        password: '',
        captcha: '',
        captchaId: '',
        areaCode: '+86',
        mobile: ''
      },
      captchaUuid: '',
      captchaUrl: '',
      isMobileLogin: false
    }
  },
  mounted() {
    this.fetchCaptcha();
    this.$store.dispatch('fetchPubConfig').then(() => {
      // 根据配置决定默认登录方式
      this.isMobileLogin = this.enableMobileRegister;
    });
  },
  methods: {
    fetchCaptcha() {
      if (this.$store.getters.getToken) {
        if (this.$route.path !== '/home') {
          this.$router.push('/home')
        }
      } else {
        this.captchaUuid = getUUID();

        Api.user.getCaptcha(this.captchaUuid, (res) => {
          if (res.status === 200) {
            const blob = new Blob([res.data], { type: res.data.type });
            this.captchaUrl = URL.createObjectURL(blob);
          } else {
            showDanger('验证码加载失败，点击刷新');
          }
        });
      }
    },

    // 切换登录方式
    switchLoginType(type) {
      this.isMobileLogin = type === 'mobile';
      // 清空表单
      this.form.username = '';
      this.form.mobile = '';
      this.form.password = '';
      this.form.captcha = '';
      this.fetchCaptcha();
    },

    // 封装输入验证逻辑
    validateInput(input, message) {
      if (!input.trim()) {
        showDanger(message);
        return false;
      }
      return true;
    },

    async login() {
      if (this.isMobileLogin) {
        // 手机号登录验证
        if (!validateMobile(this.form.mobile, this.form.areaCode)) {
          showDanger('请输入正确的手机号码');
          return;
        }
        // 拼接手机号作为用户名
        this.form.username = this.form.areaCode + this.form.mobile;
      } else {
        // 用户名登录验证
        if (!this.validateInput(this.form.username, '用户名不能为空')) {
          return;
        }
      }

      // 验证密码
      if (!this.validateInput(this.form.password, '密码不能为空')) {
        return;
      }
      // 验证验证码
      if (!this.validateInput(this.form.captcha, '验证码不能为空')) {
        return;
      }

      this.form.captchaId = this.captchaUuid
      Api.user.login(this.form, ({ data }) => {
        showSuccess('登录成功！');
        this.$store.commit('setToken', JSON.stringify(data.data));
        goToPage('/home');
      }, (err) => {
        showDanger(err.data.msg || '登录失败')
        if (err.data != null && err.data.msg != null && err.data.msg.indexOf('图形验证码') > -1) {
          this.fetchCaptcha()
        }
      })

      // 重新获取验证码
      setTimeout(() => {
        this.fetchCaptcha();
      }, 1000);
    },

    goToRegister() {
      goToPage('/register')
    },
    goToForgetPassword() {
      goToPage('/retrieve-password')
    },
  }
}
</script>
<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.login-container {
  width: 420px;
  margin: 20px;
}

.login-card {
  border-radius: 12px;
  border: 1px solid mix(white, $--color-primary, 90%);
}

.login-header {
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
    color: #37474F;
    margin: 0 0 5px 0;
  }

  .subtitle {
    font-size: 14px;
    color: #78909C;
    margin: 0;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 22px;
  }

  ::v-deep .el-input__inner {
    border-radius: 8px;

    &:focus {
      border-color: $--color-primary;
    }
  }

  ::v-deep .el-select .el-input__inner {
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
  }

  ::v-deep .el-input-group__append,
  ::v-deep .el-input-group__prepend {
    border-radius: 8px;
    border-color: #DCDFE6;
  }

  ::v-deep .el-input-group__prepend {
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
  }
}

.captcha-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;

  .el-input {
    flex-grow: 1;
  }

  .captcha-img {
    width: 120px;
    height: 40px;
    cursor: pointer;
    border-radius: 8px;
  }
}

.form-actions {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.link-text {
  font-size: 14px;
  color: $--color-primary;
  cursor: pointer;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

.login-btn {
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

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #78909C;
}

.login-type-switcher {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;

  .switcher-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #f0f2f5;
    color: #78909C;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      background-color: #e4e7ed;
    }

    &.active {
      background-color: mix($--color-primary, white, 90%);
      color: $--color-primary;
    }
  }
}

.agreement-text {
  font-size: 12px;
}
</style>
