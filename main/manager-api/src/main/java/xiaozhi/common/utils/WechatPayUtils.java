package xiaozhi.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付工具类
 */
public class WechatPayUtils {

    /**
     * 生成随机字符串
     */
    public static String generateNonceStr() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 获取当前时间戳，单位秒
     */
    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 构建JSAPI支付参数
     */
    public static Map<String, String> buildJsapiPayParams(String appId, String prepayId, PrivateKey privateKey, String serialNo) {
        String nonceStr = generateNonceStr();
        String timestamp = getCurrentTimestamp();
        String packageValue = "prepay_id=" + prepayId;
        
        String message = appId + "\n" + timestamp + "\n" + nonceStr + "\n" + packageValue + "\n";
        String signature = sign(message.getBytes(StandardCharsets.UTF_8), privateKey);
        
        Map<String, String> map = new HashMap<>();
        map.put("appId", appId);
        map.put("timeStamp", timestamp);
        map.put("nonceStr", nonceStr);
        map.put("package", packageValue);
        map.put("signType", "RSA");
        map.put("paySign", signature);
        
        return map;
    }

    /**
     * 构建APP支付参数
     */
    public static Map<String, String> buildAppPayParams(String appId, String partnerId, String prepayId, PrivateKey privateKey, String serialNo) {
        String nonceStr = generateNonceStr();
        String timestamp = getCurrentTimestamp();
        
        String message = appId + "\n" + timestamp + "\n" + nonceStr + "\n" + prepayId + "\n";
        String signature = sign(message.getBytes(StandardCharsets.UTF_8), privateKey);
        
        Map<String, String> map = new HashMap<>();
        map.put("appid", appId);
        map.put("partnerid", partnerId);
        map.put("prepayid", prepayId);
        map.put("package", "Sign=WXPay");
        map.put("noncestr", nonceStr);
        map.put("timestamp", timestamp);
        map.put("sign", signature);
        
        return map;
    }

    /**
     * 使用商户私钥签名
     */
    public static String sign(byte[] message, PrivateKey privateKey) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
            sign.update(message);
            return Base64.getEncoder().encodeToString(sign.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException("签名失败", e);
        }
    }

    /**
     * 验证微信支付回调通知签名
     */
    public static boolean verifyNotifySign(Verifier verifier, String serialNumber, String timestamp, String nonce, String body, String signature) {
        // 构造验签名串
        String message = timestamp + "\n" + nonce + "\n" + body + "\n";
        
        try {
            return verifier.verify(serialNumber, message.getBytes(StandardCharsets.UTF_8), signature);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 加载商户私钥
     */
    public static PrivateKey loadPrivateKey(String privateKeyStr) {
        if (StringUtils.isBlank(privateKeyStr)) {
            throw new IllegalArgumentException("商户私钥不能为空");
        }
        
        try {
            return PemUtil.loadPrivateKey(new ByteArrayInputStream(privateKeyStr.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("加载商户私钥失败", e);
        }
    }

    /**
     * 发送HTTP GET请求
     */
    public static String doGet(CloseableHttpClient httpClient, String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");
        
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * 发送HTTP POST请求
     */
    public static String doPost(CloseableHttpClient httpClient, String url, String requestBody) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
        
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity());
        }
    }
    
    /**
     * 生成订单号
     */
    public static String generateOrderNo() {
        return "WP" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 处理微信支付API V3通知
     * 
     * @param mchId 商户ID
     * @param apiV3Key API v3密钥
     * @param body 通知报文主体
     * @param serialNumber 证书序列号
     * @param signature 签名
     * @param nonce 随机串
     * @param timestamp 时间戳
     * @return Map<String, String> 解析后的通知数据
     */
    public static Map<String, String> parseNotification(String mchId, String apiV3Key, String body, 
                                                     String serialNumber, String signature, 
                                                     String nonce, String timestamp) throws Exception {
        // 使用更新版本的微信支付SDK处理通知
        try {
            // 获取验证器
            CertificatesManager certificatesManager = CertificatesManager.getInstance();
            Verifier verifier = certificatesManager.getVerifier(mchId);
            
            // 构建通知请求对象
            NotificationRequest request = new NotificationRequest.Builder()
                    .withSerialNumber(serialNumber)
                    .withNonce(nonce)
                    .withTimestamp(timestamp)
                    .withSignature(signature)
                    .withBody(body)
                    .build();
            
            // 创建通知处理器
            NotificationHandler handler = new NotificationHandler(verifier, apiV3Key.getBytes(StandardCharsets.UTF_8));
            
            // 验证签名并解析通知数据
            Notification notification = handler.parse(request);
            
            // 获取解密后的明文
            String plaintext = notification.getDecryptData();
            
            // 将JSON解析为Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> result = new HashMap<>();
            JsonNode rootNode = mapper.readTree(plaintext);
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getValue().isTextual()) {
                    result.put(field.getKey(), field.getValue().asText());
                } else {
                    result.put(field.getKey(), field.getValue().toString());
                }
            }
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("解析微信支付通知失败", e);
        }
    }
} 