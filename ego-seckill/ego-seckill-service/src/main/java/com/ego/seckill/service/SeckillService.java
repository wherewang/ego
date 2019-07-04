package com.ego.seckill.service;


import com.ego.seckill.vo.SeckillGoods;
import com.ego.seckill.vo.SeckillMessage;

import java.util.List;

/**
 * @Author: yaorange
 * @Time: 2019-03-30 16:56
 * @Feature:
 */
public interface SeckillService {

    /**
     * 创建订单
     * @param seckillGoods
     * @return
     */
    Long createOrder(SeckillGoods seckillGoods);


    /**
     * 检查库存
     * @param skuId
     * @return
     */
    boolean queryStock(Long skuId);

    /**
     * 发送秒杀信息到队列当中
     * @param seckillMessage
     */
    void sendMessage(SeckillMessage seckillMessage);

    /**
     * 根据用户id查询秒杀订单
     * @param userId
     * @return
     */
    Long checkSeckillOrder(Long userId);


    /**
     * 创建秒杀地址
     * @param goodsId
     * @param userId
     * @return
     */
    String createPath(Long goodsId, Long userId);

    /**
     * 验证秒杀地址
     * @param goodsId
     * @param userId
     * @param path
     * @return
     */
    boolean checkSeckillPath(Long goodsId, Long userId, String path);

    List<SeckillGoods> list();

    SeckillGoods queryById(Long id);
}
