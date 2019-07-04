package com.ego.order.service;

import com.ego.auth.entity.UserInfo;
import com.ego.common.enums.ExceptionEnum;
import com.ego.common.exception.PayException;
import com.ego.common.pojo.CartDto;
import com.ego.common.pojo.PageResult;
import com.ego.common.utils.IdWorker;
import com.ego.order.client.GoodsClient;
import com.ego.order.dto.OrderStatusEnum;
import com.ego.order.dto.PayStateEnum;
import com.ego.order.filter.LoginInterceptor;
import com.ego.order.mapper.*;
import com.ego.order.pojo.*;
import com.ego.order.utils.PayHelper;
import com.ego.seckill.vo.SeckillGoods;
import com.ego.seckill.vo.SeckillMessage;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 * @author yaorange
 * @date 2019/03/01
 */
@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private PayHelper payHelper;

    @Autowired
    private PayLogService payLogService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private PayLogMapper payLogMapper;

    @Transactional
    public Long createOrder(Orders order) {
        //生成订单ID，采用自己的算法生成订单ID
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);

        order.setCreateTime(new Date());
        order.setPostFee(0L);  //// TODO 调用物流信息，根据地址计算邮费

        //获取用户信息
        UserInfo user = LoginInterceptor.getLoginUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);  //卖家为留言


        //保存order
        orderMapper.insertSelective(order);

        order.getOrderDetails().forEach(orderDetail -> {
            orderDetail.setOrderId(orderId);
        });
        //保存detail
        orderDetailMapper.insertList(order.getOrderDetails());


        //填充orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(new Date());

        //保存orderStatus
        orderStatusMapper.insertSelective(orderStatus);

        //保存交付日志
        payLogService.createPayLog(orderId,order.getActualPay());
        //减库存
        List<CartDto> cartDTOS = new ArrayList<>();
        order.getOrderDetails().forEach(orderDetail -> {
            CartDto cartDto= new CartDto();
            cartDto.setNum(orderDetail.getNum());
            cartDto.setSkuId(orderDetail.getSkuId());
            cartDTOS.add(cartDto);
        });
        goodsClient.decreaseStock(cartDTOS);


        //删除购物车中已经下单的商品数据, 采用异步mq的方式通知购物车系统删除已购买的商品，传送商品ID和用户ID
        HashMap<String, Object> map = new HashMap<>();

        Long[] skuIds = (Long[]) cartDTOS.stream().map(cartDto -> cartDto.getSkuId()).toArray(Long[]::new);
        try {
            map.put("skuIds", skuIds);
            map.put("userId", user.getId());
            amqpTemplate.convertAndSend("ego.cart.exchange", "cart.delete", map);
        } catch (Exception e) {
            log.error("删除购物车消息发送异常，商品ID：{}",skuIds, e);
        }

        log.info("生成订单，订单编号：{}，用户id：{}", orderId, user.getId());
        return orderId;

    }

    public String generateUrl(Long orderId) {
        //根据订单ID查询订单
        Orders order = queryById(orderId);
        //判断订单状态
        if (order.getOrderStatus().getStatus() != OrderStatusEnum.INIT.value()) {
            throw new PayException(ExceptionEnum.ORDER_STATUS_EXCEPTION);
        }

        //todo 这里传入一份钱，用于测试使用，实际中使用订单中的实付金额
        String url = payHelper.createPayUrl(orderId, "易购商城测试", /*order.getActualPay()*/1L);
        if (StringUtils.isBlank(url)) {
            throw new PayException(ExceptionEnum.CREATE_PAY_URL_ERROR);
        }
        //生成支付日志
//        payLogService.createPayLog(orderId, order.getActualPay());

        return url;

    }

    public Orders queryById(Long orderId) {
        Orders order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new PayException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        order.setOrderDetails(orderDetails);
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        order.setOrderStatus(orderStatus);
        return order;
    }

    @Transactional
    public void handleNotify(Map<String, String> msg) {
        payHelper.handleNotify(msg);
    }

    public PageResult<Orders> queryOrderByPage(Integer page, Integer rows, Integer status) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        Long uid = userInfo.getId();
        //开启分页
        PageHelper.startPage(page, rows);

        List<Orders> orders =null;
        if(status == null)
        {
            //查询订单
            Orders order = new Orders();
            order.setUserId(uid);
            orders = orderMapper.select(order);
        }else{
            Example example = new Example(Orders.class);
            example.createCriteria().andEqualTo("status",status).andEqualTo("sdfsdfsd",uid);
            orders = orderMapper.selectByStatus(status, uid);
        }

        //查询订单详情
        for (Orders order : orders) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getOrderId());
            List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);

            order.setOrderDetails(orderDetailList);

            //查询订单状态
            OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(order.getOrderId());
            order.setOrderStatus(orderStatus);
        }

        PageInfo<Orders> pageInfo = new PageInfo<>(orders);

        return new PageResult<Orders>(pageInfo.getTotal(), Long.valueOf(pageInfo.getPages()), pageInfo.getList());
    }

    public void updateStatus(Long id, Integer status) {
        orderStatusMapper.updateStatus(id, status);
    }

    @Transactional
    public Long createSeckillOrder(SeckillMessage seckillMessage) {
        SeckillGoods seckillGoods = seckillMessage.getSeckillGoods();
        Orders order = new Orders();
        //生成订单ID，采用自己的算法生成订单ID
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);

        order.setCreateTime(new Date());
        order.setPostFee(0L);
        order.setActualPay(seckillGoods.getSeckillPrice());
        order.setTotalPay(seckillGoods.getPrice());
        order.setPaymentType(1);


        //获取用户信息
        UserInfo user = seckillMessage.getUserInfo();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);  //卖家为留言


        //保存order
        orderMapper.insertSelective(order);

        //保存秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderId(orderId);
        seckillOrder.setSkuId(seckillGoods.getSkuId());
        seckillOrder.setUserId(user.getId());
        seckillOrderMapper.insertSelective(seckillOrder);

        //保存detail
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        orderDetail.setImage(seckillGoods.getImage());
        orderDetail.setPrice(seckillGoods.getSeckillPrice());
        orderDetail.setNum(1);
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setTitle(seckillGoods.getTitle());

        orderDetailMapper.insert(orderDetail);


        //填充orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(new Date());

        //保存orderStatus
        orderStatusMapper.insertSelective(orderStatus);

        //保存支付日志
        PayLog payLog = new PayLog();
        payLog.setStatus(PayStateEnum.NOT_PAY.getValue());
        payLog.setPayType(1);
        payLog.setOrderId(orderId);
        payLog.setTotalFee(seckillGoods.getSeckillPrice());
        payLog.setCreateTime(new Date());
        //获取用户信息
        payLog.setUserId(user.getId());

        payLogMapper.insertSelective(payLog);
        //减库存
        CartDto cartDto= new CartDto();
        cartDto.setNum(orderDetail.getNum());
        cartDto.setSkuId(orderDetail.getSkuId());
        goodsClient.decreaseSeckillStock(cartDto);



        log.info("生成订单，订单编号：{}，用户id：{}", orderId, user.getId());
        return orderId;
    }
}
