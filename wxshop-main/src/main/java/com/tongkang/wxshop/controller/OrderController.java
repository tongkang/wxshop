package com.tongkang.wxshop.controller;

import com.tongkang.api.data.OrderInfo;
import com.tongkang.api.generator.Order;
import com.tongkang.wxshop.entity.OrderResponse;
import com.tongkang.wxshop.entity.Response;
import com.tongkang.wxshop.service.OrderService;
import com.tongkang.wxshop.service.UserContext;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Reference(version = "${wxshop.orderservice.version}")
    OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public void getOrder() {
    }

    @PostMapping("/order")
    public Response<OrderResponse> createOrder(@RequestBody OrderInfo orderInfo) {
        orderService.deductStock(orderInfo);
        return Response.of(orderService.createOrder(orderInfo, UserContext.getCurrentUser().getId()));
    }


    public void updateOrder() {
    }


    public void deleteOrder() {
    }
}
