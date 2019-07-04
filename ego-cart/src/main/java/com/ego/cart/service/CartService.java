package com.ego.cart.service;

import com.ego.auth.entity.UserInfo;
import com.ego.cart.client.GoodsClient;
import com.ego.cart.interceptor.LoginInterceptor;
import com.ego.cart.pojo.Cart;
import com.ego.common.utils.JsonUtils;
import com.ego.item.pojo.Sku;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/14
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private final static String prefix = "ego:cart:uid:";

    public void addCart(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getUser();
        //判断当前用户是否存在购物车
        BoundHashOperations<String, Object, Object> carts = redisTemplate.boundHashOps(prefix + userInfo.getId());

        //存在->判断redis中是否有该购物项
        String skuId = cart.getSkuId().toString();
        Boolean hasKey = carts.hasKey(skuId);
        Integer num = cart.getNum();

        if(hasKey)
        {
            //有->num累加
            String json = carts.get(skuId).toString();
            cart = JsonUtils.parse(json,Cart.class);
            cart.setNum(cart.getNum()+num);
        }
        else
        {
            Sku sku = goodsClient.querySkuBySkuId(cart.getSkuId()).getBody();
            //没有-->新增购物项
            cart.setUserId(userInfo.getId());
            cart.setNum(num);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setImage(sku.getImages());
            cart.setTitle(sku.getTitle());

        }
        carts.put(skuId, JsonUtils.serialize(cart));

    }

    public List<Cart> queryCart() {
        UserInfo user = LoginInterceptor.getUser();
        BoundHashOperations<String, Object, Object> carts = redisTemplate.boundHashOps(prefix + user.getId());
        return carts.values().stream()
                .map(json-> JsonUtils.parse(json.toString(),Cart.class))
                .collect(Collectors.toList());
    }

    public void updateNum(Cart cart) {
        UserInfo user = LoginInterceptor.getUser();
        BoundHashOperations<String, Object, Object> carts = redisTemplate.boundHashOps(prefix + user.getId());

        String json = carts.get(cart.getSkuId().toString()).toString();
        Integer num = cart.getNum();
        cart = JsonUtils.parse(json, Cart.class);
        cart.setNum(num);

        carts.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
    }
}
