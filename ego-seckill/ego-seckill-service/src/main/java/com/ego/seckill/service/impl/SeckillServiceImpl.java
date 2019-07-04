package com.ego.seckill.service.impl;

import com.ego.common.utils.JsonUtils;
import com.ego.item.pojo.Stock;
import com.ego.order.pojo.Orders;
import com.ego.order.pojo.OrderDetail;
import com.ego.order.pojo.SeckillOrder;
import com.ego.seckill.client.GoodsClient;
import com.ego.seckill.client.OrderClient;
import com.ego.seckill.mapper.SeckillMapper;
import com.ego.seckill.mapper.SeckillOrderMapper;
import com.ego.seckill.mapper.SkuMapper;
import com.ego.seckill.mapper.StockMapper;
import com.ego.seckill.service.SeckillService;
import com.ego.seckill.vo.SeckillGoods;
import com.ego.seckill.vo.SeckillMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Feature:
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillServiceImpl.class);

    private static final String KEY_PREFIX_PATH = "ego:seckill:path";


    /**
     * 创建订单
     * @param seckillGoods
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(SeckillGoods seckillGoods) {

        Orders order = new Orders();
        order.setPaymentType(1);
        order.setTotalPay(seckillGoods.getSeckillPrice());
        order.setActualPay(seckillGoods.getSeckillPrice());
        order.setPostFee(0L);
        order.setReceiver("李四");
        order.setReceiverMobile("15812312312");
        order.setReceiverCity("西安");
        order.setReceiverDistrict("碑林区");
        order.setReceiverState("陕西");
        order.setReceiverZip("000000000");
        order.setInvoiceType(0);
        order.setSourceType(2);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setNum(1);
        orderDetail.setTitle(seckillGoods.getTitle());
        orderDetail.setImage(seckillGoods.getImage());
        orderDetail.setPrice(seckillGoods.getSeckillPrice());
        orderDetail.setOwnSpec(this.skuMapper.selectByPrimaryKey(seckillGoods.getSkuId()).getOwnSpec());

        order.setOrderDetails(Arrays.asList(orderDetail));


        String seck = "seckill";
        ResponseEntity<List<Long>> responseEntity = this.orderClient.createOrder(seck,order);

        if (responseEntity.getStatusCode() == HttpStatus.OK){
            //库存不足，返回null
            return null;
        }
        return responseEntity.getBody().get(0);
    }

    /**
     * 检查秒杀库存
     * @param skuId
     * @return
     */
    @Override
    public boolean queryStock(Long skuId) {
        Stock stock = this.stockMapper.selectByPrimaryKey(skuId);
        if (stock.getSeckillStock() - 1 < 0){
            return false;
        }
        return true;
    }

    /**
     * 发送消息到秒杀队列当中
     * @param seckillMessage
     */
    @Override
    public void sendMessage(SeckillMessage seckillMessage) {
        String json = JsonUtils.serialize(seckillMessage);
        try {
            this.amqpTemplate.convertAndSend("order.seckill", json);
        }catch (Exception e){
            LOGGER.error("秒杀商品消息发送异常，商品id：{}",seckillMessage.getSeckillGoods().getSkuId(),e);
        }
    }

    /**
     * 根据用户id查询最新秒杀订单
     * @param userId
     * @return
     */
    @Override
    public Long checkSeckillOrder(Long userId) {
        Example example = new Example(SeckillOrder.class);
        example.createCriteria().andEqualTo("userId",userId);
        //根据id降序排序(保证最新的放在第一位)
        example.setOrderByClause(" id desc");
        List<SeckillOrder> seckillOrders = this.seckillOrderMapper.selectByExample(example);
        if (seckillOrders == null || seckillOrders.size() == 0){
            return null;
        }
        return seckillOrders.get(0).getOrderId();
    }

    /**
     * 创建秒杀地址
     * @param goodsId
     * @param userId
     * @return
     */
    @Override
    public String createPath(Long goodsId, Long userId) {

//        String str = new BCryptPasswordEncoder().encode(goodsId.toString()+ userId).replaceAll("/","");
//        String str = getMD5(goodsId, userId);-xx
        String str = UUID.randomUUID().toString();
        BoundHashOperations<String,Object,Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX_PATH);
        String key = userId.toString() + "_" + goodsId;
        hashOperations.put(key,str);
        hashOperations.expire(600, TimeUnit.SECONDS);
        return str;
    }
//    //加入一个混淆字符串(秒杀接口)的salt，为了我避免用户猜出我们的md5值，值任意给，越复杂越好
//    private final String salt="123asdzxc[];'./";
//
//    private String getMD5(long seckillGoodsId,long userId)
//    {
//        //结合秒杀的商品id与混淆字符串生成通过md5加密
//        String base=seckillGoodsId+"/"+userId+"/"+salt;
//        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
//        return md5;
//    }



    /**
     * 验证秒杀地址
     * @param goodsId
     * @param userId
     * @param path
     * @return
     */
    @Override
    public boolean checkSeckillPath(Long goodsId, Long userId, String path) {
        String key = userId.toString() + "_" + goodsId;
//        String str = getMD5(goodsId, userId);
        BoundHashOperations<String,Object,Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX_PATH);
        String encodePath = (String) hashOperations.get(key);
        //地址只能用一次，所以检验过后，在redis中删除
        hashOperations.delete(key);
        return path.equals(encodePath);
    }

    @Override
    public List<SeckillGoods> list() {
        SeckillGoods seckillGoods = new SeckillGoods();
        seckillGoods.setEnable(true);

        List<SeckillGoods> result = seckillMapper.select(seckillGoods);

        List<Stock> stocks = stockMapper.selectByIdList(result.stream().map(g -> g.getSkuId()).collect(Collectors.toList()));
        for (SeckillGoods g : result) {
            for (Stock stock : stocks) {
                if(g.getSkuId().equals(stock.getSkuId()))
                {
                    g.setStock(stock.getSeckillStock());
                    g.setSeckillTotal(stock.getSeckillTotal());
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public SeckillGoods queryById(Long id) {
        SeckillGoods seckillGoods = seckillMapper.selectByPrimaryKey(id);
        Stock stock = stockMapper.selectByPrimaryKey(seckillGoods.getSkuId());
        seckillGoods.setStock(stock.getStock());
        seckillGoods.setSeckillTotal(stock.getSeckillTotal());
        seckillGoods.setCurrentTime(new Date());
        System.out.println(seckillGoods.getCurrentTime().getTime());
        return seckillGoods;
    }
}
