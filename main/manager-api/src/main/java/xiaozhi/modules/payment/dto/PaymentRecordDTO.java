package xiaozhi.modules.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付记录DTO
 */
@Data
@Schema(description = "支付记录DTO")
public class PaymentRecordDTO {
    
    /**
     * 主键ID
     */
    @Schema(description = "主键")
    private Long id;
    
    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String orderNo;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Long productId;
    
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
     * 支付类型：WECHAT-微信支付
     */
    @Schema(description = "支付类型：WECHAT-微信支付")
    private String paymentType;
    
    /**
     * 交易类型：NATIVE-扫码支付, JSAPI-小程序支付, APP-APP支付
     */
    @Schema(description = "交易类型：NATIVE-扫码支付, JSAPI-小程序支付, APP-APP支付")
    private String tradeType;
    
    /**
     * 交易状态：NOTPAY-未支付, SUCCESS-支付成功, CLOSED-已关闭
     */
    @Schema(description = "交易状态：NOTPAY-未支付, SUCCESS-支付成功, CLOSED-已关闭")
    private String tradeState;
    
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
     * 支付发起时间
     */
    @Schema(description = "支付发起时间")
    private Date payTime;
    
    /**
     * 订单过期时间
     */
    @Schema(description = "订单过期时间")
    private Date expireTime;
    
    /**
     * 支付成功时间
     */
    @Schema(description = "支付成功时间")
    private Date successTime;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
} 