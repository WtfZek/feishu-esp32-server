package xiaozhi.modules.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 语音克隆订单响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "语音克隆订单响应信息")
public class CloneOrderResponseDTO {
    
    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String orderNo;
    
    /**
     * 二维码地址
     */
    @Schema(description = "二维码地址")
    private String codeUrl;
    
    /**
     * 订单金额
     */
    @Schema(description = "订单金额")
    private BigDecimal price;
    
    /**
     * 订单状态
     */
    @Schema(description = "订单状态")
    private String orderStatus;
    
    /**
     * 预计完成时间（毫秒）
     */
    @Schema(description = "预计完成时间，单位毫秒")
    private Long estimatedTime;
} 