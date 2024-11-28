package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author axno
 * @date 2024/11/3 22:27
 * @DESCRIPTION
 */
@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue", durable = "true"),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId){
        //1.查询订单
        Order order = orderService.getById(orderId);
        //2.判断订单的状态，未支付才进行标记
        if (order == null || order.getStatus() != 1) {
            //订单为已支付，不做处理 返回
            return;
        }
        //3.标记订单为已支付
        orderService.markOrderPaySuccess(orderId);
    }

}
