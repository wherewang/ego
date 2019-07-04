package com.ego.order.web;

import com.ego.order.pojo.Orders;
import com.ego.order.service.OrderService;
import com.ego.order.service.PayLogService;
import com.ego.common.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author yaorange
 * @date 2019/03/01
 */
@RequestMapping("order")
@RestController
@Api("订单服务接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayLogService payLogService;

    /**
     * 创建订单
     *
     * @param order
     * @return
     */
    @PostMapping
    @ApiOperation(value = "创建订单接口，返回订单编号", notes = "创建订单")
    @ApiImplicitParam(name = "order", required = true, value = "订单的json对象,包含订单条目和物流信息")
    public ResponseEntity<Long> createOrder(@RequestBody @Valid Orders order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
    }

    /**
     * 生成微信支付链接
     *
     * @param orderId
     * @return
     */
    @ApiOperation(value = "生成微信支付链接",
            notes = "微信支付链接")
    @GetMapping("url/{id}")
    public ResponseEntity<String> generateUrl(@PathVariable("id") Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.generateUrl(orderId));
    }

    /**
     * 根据订单ID查询订单详情
     *
     * @param orderId
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Orders> queryOrderById(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.queryById(orderId));
    }

    /**
     * 查询订单支付状态
     *
     * @param orderId
     * @return
     */
    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryOrderStateByOrderId(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(payLogService.queryOrderStateByOrderId(orderId));
    }

    /**
     * 分页查询所有订单
     *
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("list")
    @ApiOperation(value = "分页查询当前用户订单，并且可以根据订单状态过滤",
            notes = "分页查询当前用户订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "当前页",
                    defaultValue = "1", type = "Integer"),
            @ApiImplicitParam(name = "rows", value = "每页大小",
                    defaultValue = "5", type = "Integer"),
            @ApiImplicitParam(
                    name = "status",
                    value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })
    public ResponseEntity<PageResult<Orders>> queryOrderByPage(@RequestParam("page") Integer page,
                                                               @RequestParam("rows") Integer rows,
                                                               @RequestParam(value="status",required = false) Integer status
                                                              ) {
        return ResponseEntity.ok(orderService.queryOrderByPage(page, rows,status));
    }

    @ApiOperation(value = "更新订单状态",
            notes = "更新订单状态")
    @PutMapping("state/{id}/{status}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单编号"),
            @ApiImplicitParam(
                    name = "status",
                    value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })
    public ResponseEntity<Void> updateStatus(@PathVariable("id") Long id,
                                                              @RequestParam("status") Integer status) {
        orderService.updateStatus(id,status);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
