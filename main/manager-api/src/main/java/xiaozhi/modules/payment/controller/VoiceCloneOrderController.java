package xiaozhi.modules.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.modules.model.dto.VoiceCloneDTO;
import xiaozhi.modules.payment.dto.PaymentRequestDTO;
import xiaozhi.modules.payment.dto.PaymentResponseDTO;
import xiaozhi.modules.payment.entity.ProductEntity;
import xiaozhi.modules.payment.service.WechatPayService;
import xiaozhi.modules.payment.dao.ProductDao;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 语音克隆支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/wechat/pay/voice-clone")
@RequiredArgsConstructor
@Tag(name = "语音克隆支付接口")
@ConditionalOnProperty(prefix = "wechat.pay", name = "enabled", havingValue = "true", matchIfMissing = false)
public class VoiceCloneOrderController {
    
    private final WechatPayService wechatPayService;
    private final ProductDao productDao;

    /**
     * 创建语音克隆支付订单
     */
    @PostMapping("/order")
    @Operation(summary = "创建语音克隆支付订单")
    public Result<PaymentResponseDTO> createOrder(
            @RequestPart("voiceFile") MultipartFile voiceFile,
            @RequestParam("name") String name,
            @RequestParam("languages") String languages,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestParam(value = "tradeType", required = false) String tradeType,
            @RequestParam(value = "openid", required = false) String openid,
            HttpServletRequest request
    ) {
        try {
            // 确保不使用NATIVE支付方式
            if (tradeType == null || "NATIVE".equals(tradeType)) {
                tradeType = "H5"; // 默认使用H5支付
            }
            
            log.info("创建语音克隆订单，支付类型: {}", tradeType);
            if ("JSAPI".equals(tradeType)) {
                log.info("JSAPI支付，用户openid: {}", openid);
                if (openid == null || openid.isEmpty()) {
                    throw new RenException("JSAPI支付需要用户的openid");
                }
            }
            
            // 构建克隆DTO
            VoiceCloneDTO cloneDTO = new VoiceCloneDTO();
            cloneDTO.setName(name);
            cloneDTO.setLanguages(languages);
            cloneDTO.setRemark(remark);
            cloneDTO.setVoiceFile(voiceFile);

            // 获取客户端IP地址
            String clientIp = getClientIp(request);
            log.info("客户端IP地址: {}", clientIp);

            // 查询商品信息 - 假设ID为1的商品是语音克隆服务
            ProductEntity product = productDao.selectById(1);
            if (product == null) {
                throw new RenException("语音克隆服务商品不存在");
            }

            // 构建支付请求
            PaymentRequestDTO payRequest = new PaymentRequestDTO();
            payRequest.setProductCode("11111111111"); // 写死的商品编码
            payRequest.setTradeType(tradeType); // 根据客户端传入的支付类型设置
            payRequest.setClientIp(clientIp); // 设置客户端IP
            
            // 设置openid，用于JSAPI支付
            if (openid != null && !openid.isEmpty()) {
                payRequest.setOpenid(openid);
            }
            
            // 将克隆信息序列化为JSON并保存在attach字段中
            // 这样支付成功后可以从attach中提取克隆所需信息
            payRequest.setAttach("{\"cloneName\":\"" + name + "\",\"languages\":\"" + languages + "\",\"remark\":\"" + 
                                (remark != null ? remark : "") + "\",\"fileName\":\"" + voiceFile.getOriginalFilename() + "\"}");

            // 创建支付订单
            PaymentResponseDTO response = wechatPayService.createOrderWithFile(product, payRequest, voiceFile);
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("创建克隆支付订单失败", e);
            return ResultUtils.error("创建克隆支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单状态
     */
    @GetMapping("/order/status/{orderNo}")
    @Operation(summary = "查询订单支付状态")
    public Result<String> getOrderStatus(@PathVariable("orderNo") String orderNo) {
        try {
            String status = wechatPayService.queryOrderStatus(orderNo);
            return ResultUtils.success(status);
        } catch (Exception e) {
            log.error("查询订单状态失败", e);
            return ResultUtils.error("查询订单状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP地址
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
} 