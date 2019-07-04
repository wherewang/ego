package com.ego.order.mapper;

import com.ego.order.pojo.OrderStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author yaorange
 * @date 2019/03/01
 */
public interface OrderStatusMapper extends Mapper<OrderStatus> {
    @Update("update tb_order_status set status = #{status} where order_id=#{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
