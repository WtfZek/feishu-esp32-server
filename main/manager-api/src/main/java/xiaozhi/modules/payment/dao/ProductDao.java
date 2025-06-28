package xiaozhi.modules.payment.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xiaozhi.modules.payment.entity.ProductEntity;

/**
 * 商品DAO
 */
@Mapper
public interface ProductDao extends BaseMapper<ProductEntity> {
} 