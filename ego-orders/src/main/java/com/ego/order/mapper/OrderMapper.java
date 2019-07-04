package com.ego.order.mapper;

import com.ego.order.pojo.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author yaorange
 * @date 2019/03/01
 */
public interface OrderMapper extends Mapper<Orders>{
//    @Select("select o.* from tb_order o left join tb_order_status os on o.order_id = os.order_id where os.status = #{status} ")
    @Select("<script>" +
            "select o.* from tb_order o left join tb_order_status os on o.order_id = os.order_id where 1=1 and o.user_id = #{id}"+
            "<if test='status!=null'>"+
            "and os.status = #{status} "+
            "</if>"+
            "</script>")
    @Results({
            @Result(id=true,column="order_id",property="orderId"),
            @Result(column="total_pay",property="totalPay"),
            @Result(column="actual_pay",property="actualPay"),
            @Result(column="promotion_ids",property="paymentType"),
            @Result(column="payment_type",property="promotionIds"),
            @Result(column="post_fee",property="postFee"),
            @Result(column="create_time",property="createTime"),
            @Result(column="shipping_name",property="shippingName"),
            @Result(column="shipping_code",property="shippingCode"),
            @Result(column="user_id",property="userId"),
            @Result(column="buyer_message",property="buyerMessage"),
            @Result(column="buyer_nick",property="buyerNick"),
            @Result(column="buyer_rate",property="buyerRate"),
            @Result(column="receiver_state",property="receiver"),
            @Result(column="receiver_city",property="receiverMobile"),
            @Result(column="receiver_district",property="receiverState"),
            @Result(column="receiver_address",property="receiverCity"),
            @Result(column="receiver_mobile",property="receiverDistrict"),
            @Result(column="receiver_zip",property="receiverAddres"),
            @Result(column="receiver",property="receiverZip"),
            @Result(column="invoice_type",property="invoiceType"),
            @Result(column="source_type",property="sourceType")
    })
    List<Orders> selectByStatus(@Param("status") Integer status, @Param("id") Long id);

    @Select("select o.* from tb_order o where user_id = #{id}")
    List<Orders> queryOrdersByUid(@Param("id") Long id);
}
