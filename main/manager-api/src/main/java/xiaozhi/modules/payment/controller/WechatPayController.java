package xiaozhi.modules.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.modules.payment.dao.ProductDao;
import xiaozhi.modules.payment.dto.PaymentRequestDTO;
import xiaozhi.modules.payment.dto.PaymentResponseDTO;
import xiaozhi.modules.payment.entity.ProductEntity;
import xiaozhi.modules.payment.service.WechatPayService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 微信支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/wechat/pay")
@RequiredArgsConstructor
@Tag(name = "微信支付接口", description = "微信支付相关接口")
@ConditionalOnProperty(prefix = "wechat.pay", name = "enabled", havingValue = "true", matchIfMissing = false)
public class WechatPayController {
    
    private final WechatPayService wechatPayService;
    private final ProductDao productDao;
    
    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建支付订单", description = "创建微信支付订单")
    public Result<PaymentResponseDTO> createOrder(@RequestBody PaymentRequestDTO request) {
        try {
            // 查询商品信息
            ProductEntity product = productDao.selectById(1); // 实际项目中应该根据商品编码查询
            if (product == null) {
                throw new RenException("商品不存在");
            }
            
            // 创建支付订单
            PaymentResponseDTO response = wechatPayService.createOrder(product, request);
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("创建支付订单失败", e);
            return ResultUtils.error("创建支付订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 微信支付回调通知
     */
    @PostMapping("/notify")
    @Operation(summary = "微信支付回调通知", description = "接收微信支付结果通知")
    public ResponseEntity<Map<String, String>> payNotify(
            HttpServletRequest request,
            @RequestHeader("Wechatpay-Serial") String serialNumber,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Nonce") String nonce) {
        
        try {
            // 读取请求体
            String body = readRequestBody(request);
            log.info("接收到微信支付回调通知: {}", body);
            
            // 处理回调通知
            Map<String, String> result = wechatPayService.handlePayNotify(serialNumber, body, signature, nonce, timestamp);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("处理微信支付回调通知失败", e);
            return ResponseEntity.ok(Map.of("code", "FAIL", "message", "处理回调失败"));
        }
    }
    
    /**
     * 读取请求体
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining());
        }
    }
} 