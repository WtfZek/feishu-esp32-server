package xiaozhi.modules.payment.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

/**
 * 商品订单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("ai_order")
@Schema(description = "商品订单表")
public class OrderEntity extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 订单金额
     */
    @Schema(description = "订单金额")
    private BigDecimal amount;
    
    /**
     * 支付类型：WECHAT-微信支付，ALIPAY-支付宝
     */
    @Schema(description = "支付类型：WECHAT-微信支付，ALIPAY-支付宝")
    private String paymentType;
    
    /**
     * 交易类型：NATIVE-扫码支付, JSAPI-小程序支付, APP-APP支付
     */
    @Schema(description = "交易类型：NATIVE-扫码支付, JSAPI-小程序支付, APP-APP支付")
    private String tradeType;
    
    /**
     * 订单状态：UNPAID-未支付, PAID-已支付, SHIPPED-已发货, COMPLETED-已完成, CANCELLED-已取消
     */
    @Schema(description = "订单状态：UNPAID-未支付, PAID-已支付, SHIPPED-已发货, COMPLETED-已完成, CANCELLED-已取消")
    private String orderStatus;
    
    /**
     * 支付状态：NOTPAY-未支付, SUCCESS-支付成功, CLOSED-已关闭
     */
    @Schema(description = "支付状态：NOTPAY-未支付, SUCCESS-支付成功, CLOSED-已关闭")
    private String paymentStatus;
    
    /**
     * 微信支付订单号
     */
    @Schema(description = "微信支付订单号")
    private String transactionId;
    
    /**
     * 预支付交易会话标识
     */
    @Schema(description = "预支付交易会话标识")
    private String prepayId;
    
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
     * 收货地址
     */
    @Schema(description = "收货地址")
    private String address;
    
    /**
     * 收货人
     */
    @Schema(description = "收货人")
    private String receiver;
    
    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;
    
    /**
     * 订单备注
     */
    @Schema(description = "订单备注")
    private String remark;
    
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
     * 发货时间
     */
    @Schema(description = "发货时间")
    private Date shipTime;
    
    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private Date completeTime;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createDate;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateDate;
} 