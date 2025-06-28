package xiaozhi.common.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

/**
 * 微信支付配置类
 */
@Data
@Slf4j
@Component
@Configuration
@ConditionalOnProperty(prefix = "wechat.pay", name = "enabled", havingValue = "true", matchIfMissing = false)
public class WechatPayConfig {

    /**
     * 微信支付应用ID
     */
    @Value("${wechat.pay.app-id}")
    private String appId;

    /**
     * 微信支付商户号
     */
    @Value("${wechat.pay.mch-id}")
    private String mchId;

    /**
     * 商户API私钥路径
     */
    @Value("${wechat.pay.private-key-path}")
    private String privateKeyPath;

    /**
     * 商户API证书序列号
     */
    @Value("${wechat.pay.mch-serial-no}")
    private String mchSerialNo;

    /**
     * API v3密钥
     */
    @Value("${wechat.pay.api-v3-key}")
    private String apiV3Key;

    /**
     * 微信支付回调通知地址
     */
    @Value("${wechat.pay.notify-url}")
    private String notifyUrl;

    /**
     * 支付结果通知地址
     */
    @Value("${wechat.pay.wechat-pay-certificate-path:classpath:cert/wechatpay_certificate.pem}")
    private String wechatPayCertificatePath;

    /**
     * 公众号APPID
     */
    @Value("${wechat.pay.app-id-config.mp:#{null}}")
    private String mpAppId;

    /**
     * 小程序APPID
     */
    @Value("${wechat.pay.app-id-config.miniapp:#{null}}")
    private String miniappAppId;

    /**
     * APP APPID
     */
    @Value("${wechat.pay.app-id-config.app:#{null}}")
    private String appAppId;
    
    /**
     * 应用URL，用于H5支付场景信息
     */
    @Value("${wechat.pay.app-url:https://192.168.3.188:5173/}")
    private String appUrl;

    /**
     * 获取商户私钥
     */
    public PrivateKey getPrivateKey() {
        try {
            ClassPathResource resource = new ClassPathResource(privateKeyPath.replace("classpath:", ""));
            String privateKey = new String(StreamUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
            return PemUtil.loadPrivateKey(privateKey);
        } catch (IOException e) {
            throw new RuntimeException("加载商户私钥文件失败", e);
        }
    }

    /**
     * 微信支付HTTP客户端
     */
    @Bean
    public CloseableHttpClient wechatPayHttpClient() throws IOException {
        try {
            // 获取商户私钥
            PrivateKey privateKey = getPrivateKey();
            
            // 创建证书管理器
            CertificatesManager certificatesManager = CertificatesManager.getInstance();
            // 向证书管理器增加需要自动更新平台证书的商户信息
            certificatesManager.putMerchant(mchId, new WechatPay2Credentials(mchId, 
                    new PrivateKeySigner(mchSerialNo, privateKey)), apiV3Key.getBytes(StandardCharsets.UTF_8));
            
            // 从证书管理器获取验证器，验证器会自动获取并更新微信支付平台证书
            Verifier verifier = certificatesManager.getVerifier(mchId);
            
            // 构建HTTP客户端
            return WechatPayHttpClientBuilder.create()
                    .withMerchant(mchId, mchSerialNo, privateKey)
                    .withValidator(new WechatPay2Validator(verifier))
                    .build();
        } catch (Exception e) {
            log.error("创建微信支付HTTP客户端失败", e);
            throw new RuntimeException("创建微信支付HTTP客户端失败", e);
        }
    }

    @PostConstruct
    public void init() {
        log.info("微信支付配置初始化成功: appId={}, mchId={}", appId, mchId);
    }
} 