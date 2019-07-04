package com.ego.seckill.client;

import com.ego.order.api.OrderApi;
import com.ego.seckill.config.OrderConfig;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: yaorange
 * @Time: 2018-11-12 15:19
 * @Feature: 订单接口
 */
@FeignClient(value = "order-service",configuration = OrderConfig.class)
public interface OrderClient extends OrderApi {
}
