package xiaozhi.modules.payment.dto;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 支付请求DTO
 */
@Data
@Schema(description = "支付请求DTO")
public class PaymentRequestDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品编码
     */
    @Schema(description = "商品编码", required = true)
    @NotBlank(message = "商品编码不能为空")
    private String productCode;
    
    /**
     * 交易类型：NATIVE-扫码支付, JSAPI-小程序支付, APP-APP支付
     */
    @Schema(description = "交易类型：NATIVE-扫码支付, JSAPI-小程序支付, APP-APP支付", required = true)
    @NotBlank(message = "交易类型不能为空")
    private String tradeType;
    
    /**
     * 用户openid，JSAPI支付时必填
     */
    @Schema(description = "用户openid，JSAPI支付时必填")
    private String openid;
    
    /**
     * 客户端IP
     */
    @Schema(description = "客户端IP")
    private String clientIp;
    
    /**
     * 附加数据，在查询API和支付通知中原样返回
     */
    @Schema(description = "附加数据，在查询API和支付通知中原样返回")
    private String attach;
} 