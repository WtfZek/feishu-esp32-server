SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `xiaozhi_esp32_server_dev`.`ai_agent` ADD COLUMN `agent_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '智能体头像' AFTER `agent_name`;

ALTER TABLE `xiaozhi_esp32_server_dev`.`ai_agent` ADD COLUMN `published` tinyint NULL DEFAULT NULL COMMENT '是否已发布（1已发布 0未发布）' AFTER `language`;

ALTER TABLE `xiaozhi_esp32_server_dev`.`ai_model_provider` COMMENT = '模型供应器配置表';

CREATE TABLE `xiaozhi_esp32_server_dev`.`ai_order`  (
                                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                        `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                                                        `user_id` bigint NOT NULL COMMENT '用户ID',
                                                        `product_id` bigint NOT NULL COMMENT '商品ID',
                                                        `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
                                                        `amount` decimal(10, 2) NOT NULL COMMENT '订单金额',
                                                        `payment_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付类型：WECHAT-微信支付，ALIPAY-支付宝',
                                                        `trade_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易类型：NATIVE-扫码支付, JSAPI-小程序支付, APP-APP支付',
                                                        `order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UNPAID' COMMENT '订单状态：UNPAID-未支付, PAID-已支付, SHIPPED-已发货, COMPLETED-已完成, CANCELLED-已取消',
                                                        `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NOTPAY' COMMENT '支付状态：NOTPAY-未支付, SUCCESS-支付成功, CLOSED-已关闭',
                                                        `transaction_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信支付订单号',
                                                        `prepay_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预支付交易会话标识',
                                                        `code_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '二维码链接(NATIVE支付时返回)',
                                                        `h5_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'H5支付链接(H5支付时返回)',
                                                        `app_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'APP支付参数(APP支付时返回)',
                                                        `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
                                                        `receiver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人',
                                                        `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
                                                        `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单备注',
                                                        `pay_time` datetime NULL DEFAULT NULL COMMENT '支付发起时间',
                                                        `expire_time` datetime NOT NULL COMMENT '订单过期时间',
                                                        `success_time` datetime NULL DEFAULT NULL COMMENT '支付成功时间',
                                                        `ship_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
                                                        `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
                                                        `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                        `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                        PRIMARY KEY (`id`) USING BTREE,
                                                        UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
                                                        INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                                        INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
                                                        INDEX `idx_order_status`(`order_status` ASC) USING BTREE,
                                                        INDEX `idx_payment_status`(`payment_status` ASC) USING BTREE,
                                                        INDEX `idx_create_date`(`create_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品订单表' ROW_FORMAT = Dynamic;

CREATE TABLE `xiaozhi_esp32_server_dev`.`ai_product`  (
                                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                          `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
                                                          `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编码',
                                                          `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
                                                          `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
                                                          `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '商品状态：0-下架，1-上架',
                                                          `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
                                                          `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                          `updater` bigint NULL DEFAULT NULL COMMENT '更新者',
                                                          `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                          PRIMARY KEY (`id`) USING BTREE,
                                                          UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
                                                          INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS=1;