package xiaozhi.modules.payment.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品DTO
 */
@Data
@Schema(description = "商品DTO")
public class ProductDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @Schema(description = "主键")
    private Long id;
    
    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String productName;
    
    /**
     * 商品编码
     */
    @Schema(description = "商品编码")
    private String productCode;
    
    /**
     * 商品价格
     */
    @Schema(description = "商品价格")
    private BigDecimal price;
    
    /**
     * 商品描述
     */
    @Schema(description = "商品描述")
    private String description;
    
    /**
     * 状态：1-启用，0-禁用
     */
    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;
} 