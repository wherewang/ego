package com.ego.seckill.client;

import com.ego.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: yaorange
 * Time: 2018-10-11 20:50
 * Feature:商品FeignClient
 */
@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {
}
