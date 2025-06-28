package xiaozhi.common.constant;

/**
 * 支付常量
 */
public class PaymentConstant {

    /**
     * 支付类型
     */
    public static class PaymentType {
        /**
         * 微信支付
         */
        public static final String WECHAT = "WECHAT";
        
        /**
         * 支付宝支付
         */
        public static final String ALIPAY = "ALIPAY";
    }
    
    /**
     * 交易类型
     */
    public static class TradeType {
        /**
         * 扫码支付
         */
        public static final String NATIVE = "NATIVE";
        
        /**
         * 小程序支付
         */
        public static final String JSAPI = "JSAPI";
        
        /**
         * APP支付
         */
        public static final String APP = "APP";
        
        /**
         * H5支付
         */
        public static final String H5 = "H5";
    }
    
    /**
     * 交易状态
     */
    public static class TradeState {
        /**
         * 未支付
         */
        public static final String NOTPAY = "NOTPAY";
        
        /**
         * 支付成功
         */
        public static final String SUCCESS = "SUCCESS";
        
        /**
         * 已关闭
         */
        public static final String CLOSED = "CLOSED";
        
        /**
         * 已撤销
         */
        public static final String REVOKED = "REVOKED";
        
        /**
         * 用户支付中
         */
        public static final String USERPAYING = "USERPAYING";
        
        /**
         * 支付失败
         */
        public static final String PAYERROR = "PAYERROR";
    }
    
    /**
     * 订单状态
     */
    public static class OrderStatus {
        /**
         * 未支付
         */
        public static final String UNPAID = "UNPAID";
        
        /**
         * 已支付
         */
        public static final String PAID = "PAID";
        
        /**
         * 已发货
         */
        public static final String SHIPPED = "SHIPPED";
        
        /**
         * 已完成
         */
        public static final String COMPLETED = "COMPLETED";
        
        /**
         * 已取消
         */
        public static final String CANCELLED = "CANCELLED";
    }
    
    /**
     * 微信支付API地址
     */
    public static class WechatPayApiUrl {
        /**
         * 扫码支付
         */
        public static final String NATIVE_PAY = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
        
        /**
         * 小程序支付
         */
        public static final String JSAPI_PAY = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
        
        /**
         * APP支付
         */
        public static final String APP_PAY = "https://api.mch.weixin.qq.com/v3/pay/transactions/app";
        
        /**
         * H5支付
         */
        public static final String H5_PAY = "https://api.mch.weixin.qq.com/v3/pay/transactions/h5";
        
        /**
         * 订单查询
         */
        public static final String ORDER_QUERY = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/";
        
        /**
         * 关闭订单
         */
        public static final String CLOSE_ORDER = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/%s/close";
        
        /**
         * 申请退款
         */
        public static final String REFUND = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";
        
        /**
         * 查询退款
         */
        public static final String REFUND_QUERY = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/";
    }
} 