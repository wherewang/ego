package com.ego.order.api;

import com.ego.order.pojo.Orders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Author: yaorange
 * @Time: 2018-11-12 15:13
 * @Feature: 订单服务接口
 */
public interface OrderApi {

    /**
     * 创建订单
     * @param seck
     * @param order
     * @return
     */
    @PostMapping
    ResponseEntity<List<Long>> createOrder(@RequestParam("seck") String seck, @RequestBody @Valid Orders order);


    /**
     * 修改订单状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping("{id}/{status}")
    ResponseEntity<Boolean> updateOrderStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status);
}
