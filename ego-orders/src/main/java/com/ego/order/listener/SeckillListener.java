package com.ego.order.listener;


import com.ego.common.utils.JsonUtils;
import com.ego.order.service.OrderService;
import com.ego.seckill.vo.SeckillMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/4/17
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Slf4j
@Component
public class SeckillListener {
    @Autowired
    private OrderService orderService;

    /**
     * 处理秒杀订单
     *
     * @param json
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ego.order.seckill.queue", durable = "true"),
            exchange = @Exchange(
                    value = "ego.order.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"order.seckill"}))
    public void listenCreate(String json) throws Exception {
        log.info("开始创建秒杀订单:{}",json);
        if (json == null) {
            return;
        }
        try
        {
            SeckillMessage seckillMessage = JsonUtils.parse(json, SeckillMessage.class);
            orderService.createSeckillOrder(seckillMessage);
        }catch (Exception e)
        {
            log.error("创建秒杀订单失败",e);
        }

    }
}
