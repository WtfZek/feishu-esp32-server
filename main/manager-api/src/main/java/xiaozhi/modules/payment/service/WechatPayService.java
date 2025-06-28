package xiaozhi.modules.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xiaozhi.common.config.WechatPayConfig;
import xiaozhi.common.constant.PaymentConstant;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.WechatPayUtils;
import xiaozhi.modules.model.service.VoiceCloneService;
import xiaozhi.modules.payment.dao.OrderDao;
import xiaozhi.modules.payment.dto.PaymentRequestDTO;
import xiaozhi.modules.payment.dto.PaymentResponseDTO;
import xiaozhi.modules.payment.entity.OrderEntity;
import xiaozhi.modules.payment.entity.ProductEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "wechat.pay", name = "enabled", havingValue = "true", matchIfMissing = false)
public class WechatPayService {
    
    private final WechatPayConfig wechatPayConfig;
    private final CloseableHttpClient wechatPayHttpClient;
    private final ObjectMapper objectMapper;
    private final OrderDao orderDao; // 注入订单DAO
    private final VoiceCloneService voiceCloneService; // 注入语音克隆服务
    
    /**
     * 创建微信支付订单
     * @param product 商品信息
     * @param request 支付请求信息
     * @return 支付响应信息
     */
    public PaymentResponseDTO createOrder(ProductEntity product, PaymentRequestDTO request) {
        try {
            // 确保交易类型不为空
            if (request.getTradeType() == null) {
                request.setTradeType("H5"); // 默认使用H5支付
            }
            
            log.info("创建支付订单，商品：{}，交易类型：{}", product.getName(), request.getTradeType());
            
            // 构建支付参数
            ObjectNode reqData = objectMapper.createObjectNode();
            reqData.put("appid", wechatPayConfig.getAppId());
            reqData.put("mchid", wechatPayConfig.getMchId());
            reqData.put("description", product.getName());
            reqData.put("out_trade_no", WechatPayUtils.generateOrderNo());
            reqData.put("notify_url", wechatPayConfig.getNotifyUrl());
            
            // 订单金额
            ObjectNode amount = objectMapper.createObjectNode();
            amount.put("total", product.getPrice().multiply(new BigDecimal("100")).intValue());
            amount.put("currency", "CNY");
            reqData.set("amount", amount);
            
            // 附加数据
            if (request.getAttach() != null) {
                reqData.put("attach", request.getAttach());
            }
            
            // 根据交易类型选择不同的支付API地址
            String apiUrl;
            PaymentResponseDTO response = PaymentResponseDTO.builder()
                    .orderNo(reqData.get("out_trade_no").asText())
                    .productName(product.getName())
                    .amount(product.getPrice())
                    .tradeType(request.getTradeType())
                    .build();
            
            switch (request.getTradeType()) {
                case PaymentConstant.TradeType.NATIVE:
                    apiUrl = PaymentConstant.WechatPayApiUrl.NATIVE_PAY;
                    break;
                case PaymentConstant.TradeType.JSAPI:
                    apiUrl = PaymentConstant.WechatPayApiUrl.JSAPI_PAY;
                    // JSAPI支付需要用户的openid
                    if (request.getOpenid() == null) {
                        throw new RuntimeException("JSAPI支付需要用户的openid");
                    }
                    ObjectNode payer = objectMapper.createObjectNode();
                    payer.put("openid", request.getOpenid());
                    reqData.set("payer", payer);
                    break;
                case PaymentConstant.TradeType.APP:
                    apiUrl = PaymentConstant.WechatPayApiUrl.APP_PAY;
                    break;
                case "H5":
                    apiUrl = PaymentConstant.WechatPayApiUrl.H5_PAY;
                    // H5支付需要场景信息
                    ObjectNode sceneInfo = objectMapper.createObjectNode();
                    ObjectNode h5Info = objectMapper.createObjectNode();
                    h5Info.put("type", "Wap");
                    h5Info.put("app_name", "语音克隆");
                    h5Info.put("app_url", wechatPayConfig.getAppUrl());
                    sceneInfo.set("h5_info", h5Info);
                    // 添加用户终端IP，这是H5支付必需的
                    String clientIp = request.getClientIp();
                    if (clientIp == null || clientIp.isEmpty()) {
                        clientIp = "127.0.0.1"; // 默认IP，如果没有提供
                    }
                    sceneInfo.put("payer_client_ip", clientIp);
                    reqData.set("scene_info", sceneInfo);
                    break;
                default:
                    throw new RuntimeException("不支持的交易类型: " + request.getTradeType());
            }
            
            // 发送请求
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(reqData.toString(), StandardCharsets.UTF_8));
            
            // 记录请求参数
            log.info("发送微信支付API请求: URL={}, 请求参数={}", apiUrl, reqData.toString());
            
            try (CloseableHttpResponse wxResponse = wechatPayHttpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(wxResponse.getEntity());
                log.info("收到微信支付API响应: statusCode={}, 响应内容={}", wxResponse.getStatusLine().getStatusCode(), responseBody);
                
                JsonNode responseNode = objectMapper.readTree(responseBody);
                
                // 检查API响应是否包含错误信息
                if (responseNode.has("code") && responseNode.has("message")) {
                    String errorCode = responseNode.get("code").asText();
                    String errorMessage = responseNode.get("message").asText();
                    log.error("微信支付API返回错误: code={}, message={}", errorCode, errorMessage);
                    throw new RuntimeException("微信支付创建订单失败: " + errorMessage);
                }
                
                // 根据交易类型处理返回结果
                switch (request.getTradeType()) {
                    case PaymentConstant.TradeType.NATIVE:
                        JsonNode codeUrlNode = responseNode.get("code_url");
                        if (codeUrlNode != null) {
                            response.setCodeUrl(codeUrlNode.asText());
                        } else {
                            // 处理错误，打印响应信息以便调试
                            log.error("微信支付创建订单失败，返回数据缺少code_url字段: {}", responseBody);
                            throw new RuntimeException("微信支付创建订单失败，返回数据格式异常");
                        }
                        break;
                    case PaymentConstant.TradeType.JSAPI:
                        JsonNode prepayIdNode = responseNode.get("prepay_id");
                        if (prepayIdNode != null) {
                            response.setPrepayId(prepayIdNode.asText());
                            // 构建小程序调起支付参数
                            PrivateKey privateKey = wechatPayConfig.getPrivateKey();
                            Map<String, String> payParams = WechatPayUtils.buildJsapiPayParams(
                                    wechatPayConfig.getAppId(), 
                                    response.getPrepayId(), 
                                    privateKey, 
                                    wechatPayConfig.getMchSerialNo());
                            
                            PaymentResponseDTO.MiniAppPayParams miniAppParams = PaymentResponseDTO.MiniAppPayParams.builder()
                                    .appId(payParams.get("appId"))
                                    .timeStamp(payParams.get("timeStamp"))
                                    .nonceStr(payParams.get("nonceStr"))
                                    .packageValue(payParams.get("package"))
                                    .signType(payParams.get("signType"))
                                    .paySign(payParams.get("paySign"))
                                    .build();
                            
                            response.setMiniAppParams(miniAppParams);
                        } else {
                            log.error("微信支付创建订单失败，返回数据缺少prepay_id字段: {}", responseBody);
                            throw new RuntimeException("微信支付创建订单失败，返回数据格式异常");
                        }
                        break;
                    case PaymentConstant.TradeType.APP:
                        JsonNode appPrepayIdNode = responseNode.get("prepay_id");
                        if (appPrepayIdNode != null) {
                            response.setPrepayId(appPrepayIdNode.asText());
                            // 构建APP调起支付参数
                            PrivateKey privateKey2 = wechatPayConfig.getPrivateKey();
                            Map<String, String> appPayParams = WechatPayUtils.buildAppPayParams(
                                    wechatPayConfig.getAppId(), 
                                    wechatPayConfig.getMchId(), 
                                    response.getPrepayId(), 
                                    privateKey2, 
                                    wechatPayConfig.getMchSerialNo());
                            
                            // 将APP支付参数转换为JSON字符串
                            response.setAppUrl(objectMapper.writeValueAsString(appPayParams));
                        } else {
                            log.error("微信支付创建订单失败，返回数据缺少prepay_id字段: {}", responseBody);
                            throw new RuntimeException("微信支付创建订单失败，返回数据格式异常");
                        }
                        break;
                }
                
                // 保存订单记录
                saveOrder(response, product, request);
                
                return response;
            }
        } catch (Exception e) {
            log.error("创建微信支付订单失败", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 创建带文件上传的微信支付订单 - 用于语音克隆服务
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponseDTO createOrderWithFile(ProductEntity product, PaymentRequestDTO request, MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                throw new RenException("上传文件不能为空");
            }
            
            // 保存上传的文件到临时目录
            String tempFilePath = saveFileToTemp(file);
            
            // 使用普通的订单创建方法创建支付订单
            PaymentResponseDTO response = createOrder(product, request);
            
            // 检查订单是否创建成功
            if (response == null || response.getOrderNo() == null) {
                throw new RuntimeException("支付订单创建失败");
            }
            
            // 在订单表添加文件路径字段，方便后续处理
            OrderEntity order = orderDao.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderEntity>()
                    .eq("order_no", response.getOrderNo()));
            if (order != null) {
                order.setRemark(order.getRemark() + "|FILE_PATH:" + tempFilePath);
                orderDao.updateById(order);
            } else {
                log.warn("创建订单成功但未找到订单记录, orderNo: {}", response.getOrderNo());
            }
            
            return response;
        } catch (Exception e) {
            log.error("创建带文件的支付订单失败", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 查询订单状态
     */
    public String queryOrderStatus(String orderNo) {
        OrderEntity order = orderDao.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderEntity>()
                .eq("order_no", orderNo));
        if (order == null) {
            throw new RenException("订单不存在");
        }
        return order.getPaymentStatus();
    }
    
    /**
     * 保存订单记录
     */
    private void saveOrder(PaymentResponseDTO response, ProductEntity product, PaymentRequestDTO request) {
        // 在实际项目中，这里会调用DAO保存订单记录到数据库
        OrderEntity order = OrderEntity.builder()
                .orderNo(response.getOrderNo())
                .productId(product.getId())
                .productName(product.getName())
                .amount(product.getPrice())
                .paymentType(PaymentConstant.PaymentType.WECHAT)
                .tradeType(request.getTradeType())
                .orderStatus(PaymentConstant.OrderStatus.UNPAID)
                .paymentStatus(PaymentConstant.TradeState.NOTPAY)
                .codeUrl(response.getCodeUrl())
                .h5Url(response.getH5Url())
                .appUrl(response.getAppUrl())
                .prepayId(response.getPrepayId())
                .payTime(new Date())
                .expireTime(new Date(System.currentTimeMillis() + 30 * 60 * 1000)) // 30分钟有效期
                .createDate(new Date())
                .updateDate(new Date())
                .build();
        
        // 附加数据
        if (request.getAttach() != null) {
            order.setRemark(request.getAttach());
        }
        
        // 保存订单
        orderDao.insert(order);
    }
    
    /**
     * 将文件保存到临时目录
     */
    private String saveFileToTemp(MultipartFile file) throws IOException {
        // 创建临时目录
        String tmpDir = System.getProperty("java.io.tmpdir");
        File targetDir = new File(tmpDir + "/voice_clone_files");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        File targetFile = new File(targetDir, fileName);
        
        // 保存文件
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(file.getBytes());
        }
        
        return targetFile.getAbsolutePath();
    }
    
    /**
     * 处理微信支付回调通知
     */
    public Map<String, String> handlePayNotify(String serialNumber, String body, String signature, String nonce, String timestamp) {
        try {
            // 使用工具类解析通知
            Map<String, String> notifyData = WechatPayUtils.parseNotification(
                    wechatPayConfig.getMchId(),
                    wechatPayConfig.getApiV3Key(), 
                    body, 
                    serialNumber, 
                    signature, 
                    nonce, 
                    timestamp);
            
            // 获取关键信息
            String outTradeNo = notifyData.get("out_trade_no");
            String tradeState = notifyData.get("trade_state");
            String transactionId = notifyData.get("transaction_id");
            
            // 处理支付成功回调
            if ("SUCCESS".equals(tradeState)) {
                // 查询订单
                OrderEntity order = orderDao.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderEntity>()
                        .eq("order_no", outTradeNo));
                
                if (order != null && PaymentConstant.TradeState.NOTPAY.equals(order.getPaymentStatus())) {
                    // 更新订单状态
                    order.setPaymentStatus(PaymentConstant.TradeState.SUCCESS);
                    order.setOrderStatus(PaymentConstant.OrderStatus.PAID);
                    order.setTransactionId(transactionId);
                    order.setSuccessTime(new Date());
                    orderDao.updateById(order);
                    
                    // 如果是语音克隆服务，异步启动克隆任务
                    if (order.getProductId() == 1) { // 假设ID为1的商品是语音克隆服务
                        processVoiceCloneOrder(order);
                    }
                }
            }
            
            // 返回成功响应
            Map<String, String> result = new HashMap<>();
            result.put("code", "SUCCESS");
            result.put("message", "成功");
            return result;
        } catch (Exception e) {
            log.error("处理微信支付回调通知失败", e);
            throw new RuntimeException("处理微信支付回调通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理语音克隆订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void processVoiceCloneOrder(OrderEntity order) {
        try {
            // 解析订单的附加数据和文件路径
            String remark = order.getRemark();
            String filePath = null;
            
            // 提取文件路径
            if (remark.contains("|FILE_PATH:")) {
                int index = remark.indexOf("|FILE_PATH:");
                filePath = remark.substring(index + 11);
                remark = remark.substring(0, index);
            }
            
            // 解析附加数据
            JsonNode attachJson = objectMapper.readTree(remark);
            String cloneName = attachJson.get("cloneName").asText();
            String languages = attachJson.get("languages").asText();
            String cloneRemark = attachJson.has("remark") ? attachJson.get("remark").asText() : "";
            
            // 调用语音克隆服务
            if (filePath != null) {
                File audioFile = new File(filePath);
                if (audioFile.exists()) {
                    // 调用语音克隆服务
                    Result<?> result = voiceCloneService.processCloneFromFile(cloneName, languages, cloneRemark, audioFile, order.getUserId());
                    
                    if (result.getCode() == 0) {
                        // 更新订单状态
                        order.setOrderStatus(PaymentConstant.OrderStatus.COMPLETED);
                        orderDao.updateById(order);
                        log.info("语音克隆任务处理成功，订单号: {}", order.getOrderNo());
                    } else {
                        log.error("语音克隆任务处理失败，订单号: {}, 错误信息: {}", order.getOrderNo(), result.getMsg());
                    }
                } else {
                    log.error("语音克隆音频文件不存在，订单号: {}, 文件路径: {}", order.getOrderNo(), filePath);
                }
            } else {
                log.error("订单中未找到音频文件路径，订单号: {}", order.getOrderNo());
            }
        } catch (Exception e) {
            log.error("处理语音克隆订单失败, 订单号: {}", order.getOrderNo(), e);
        }
    }
} 