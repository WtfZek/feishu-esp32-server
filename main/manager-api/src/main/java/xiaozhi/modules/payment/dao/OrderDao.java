package xiaozhi.modules.payment.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xiaozhi.modules.payment.entity.OrderEntity;

/**
 * 商品订单DAO接口
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
} 