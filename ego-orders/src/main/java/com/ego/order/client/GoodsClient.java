package com.ego.order.client;

import com.ego.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author yaorange
 * @date 2019/03/01
 */
@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {


}
