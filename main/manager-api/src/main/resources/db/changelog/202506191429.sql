SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE `sys_feedback`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈类型',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈内容',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `creator` bigint NOT NULL COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_creator`(`creator` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户意见反馈表' ROW_FORMAT = DYNAMIC;



SET FOREIGN_KEY_CHECKS=1;

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO `xiaozhi_esp32_server`.`sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `remark`, `sort`, `creator`, `create_date`, `updater`, `update_date`) VALUES (102039, 103, '问题反馈', '1', '问题反馈', 1, 1, '2025-06-19 14:33:40', 1, '2025-06-19 14:33:43');

INSERT INTO `xiaozhi_esp32_server`.`sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `remark`, `sort`, `creator`, `create_date`, `updater`, `update_date`) VALUES (102040, 103, '功能建议', '2', '功能建议', 2, 1, '2025-06-19 14:33:40', 1, '2025-06-19 14:33:43');

INSERT INTO `xiaozhi_esp32_server`.`sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `remark`, `sort`, `creator`, `create_date`, `updater`, `update_date`) VALUES (102041, 103, '其他', '0', '其他反馈', 99, 1, '2025-06-19 14:35:38', 1, '2025-06-19 14:35:40');

SET FOREIGN_KEY_CHECKS = 1;