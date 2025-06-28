package xiaozhi.modules.payment.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 支付响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付响应DTO")
public class PaymentResponseDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String orderNo;
    
    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String productName;
    
    /**
     * 支付金额
     */
    @Schema(description = "支付金额")
    private BigDecimal amount;
    
    /**
     * 交易类型：NATIVE-扫码支付, JSAPI-小程序支付, APP-APP支付
     */
    @Schema(description = "交易类型：NATIVE-扫码支付, JSAPI-小程序支付, APP-APP支付")
    private String tradeType;
    
    /**
     * 二维码链接(NATIVE支付时返回)
     */
    @Schema(description = "二维码链接(NATIVE支付时返回)")
    private String codeUrl;
    
    /**
     * H5支付链接(H5支付时返回)
     */
    @Schema(description = "H5支付链接(H5支付时返回)")
    private String h5Url;
    
    /**
     * APP支付参数(APP支付时返回)
     */
    @Schema(description = "APP支付参数(APP支付时返回)")
    private String appUrl;
    
    /**
     * 预支付交易会话标识
     */
    @Schema(description = "预支付交易会话标识")
    private String prepayId;
    
    /**
     * 小程序支付所需参数
     */
    @Schema(description = "小程序支付所需参数")
    private MiniAppPayParams miniAppParams;
    
    /**
     * 小程序支付参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "小程序支付参数")
    public static class MiniAppPayParams {
        
        /**
         * 应用ID
         */
        @Schema(description = "应用ID")
        private String appId;
        
        /**
         * 时间戳
         */
        @Schema(description = "时间戳")
        private String timeStamp;
        
        /**
         * 随机字符串
         */
        @Schema(description = "随机字符串")
        private String nonceStr;
        
        /**
         * 订单详情扩展字符串
         */
        @Schema(description = "订单详情扩展字符串")
        private String packageValue;
        
        /**
         * 签名方式
         */
        @Schema(description = "签名方式")
        private String signType;
        
        /**
         * 签名
         */
        @Schema(description = "签名")
        private String paySign;
    }
} 