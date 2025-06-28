package xiaozhi.modules.payment.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

/**
 * 商品实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_product")
@Schema(description = "商品表")
public class ProductEntity extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;
    
    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String name;
    
    /**
     * 商品编码
     */
    @Schema(description = "商品编码")
    private String code;
    
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
     * 商品状态：0-下架，1-上架
     */
    @Schema(description = "商品状态：0-下架，1-上架")
    private Integer status;

} 