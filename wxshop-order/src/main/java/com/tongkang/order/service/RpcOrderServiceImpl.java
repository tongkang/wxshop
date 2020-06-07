package com.tongkang.order.service;

import com.tongkang.api.data.OrderInfo;
import com.tongkang.api.generator.Order;
import com.tongkang.api.generator.OrderMapper;
import com.tongkang.api.rpc.OrderService;
import com.tongkang.order.mapper.MyOrderMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.function.BooleanSupplier;

import static com.tongkang.api.DataStatus.PENDING;

@Service(version = "${wxshop.orderservice.version}")
public class RpcOrderServiceImpl implements OrderService {

    private OrderMapper orderMapper;
    private MyOrderMapper myOrderMapper;

    @Autowired
    public RpcOrderServiceImpl(OrderMapper orderMapper, MyOrderMapper myOrderMapper) {
        this.orderMapper = orderMapper;
        this.myOrderMapper = myOrderMapper;
    }

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        insertOrder(order);
        orderInfo.setOrderId(order.getId());
        myOrderMapper.insertOrders(orderInfo);
        return order;
    }

    private void insertOrder(Order order) {
        order.setStatus(PENDING.getName());

        verify(() -> order.getUserId() == null, "userId不能为空");
        verify(() -> order.getTotalPrice() == null || order.getTotalPrice().doubleValue() < 0, "totalPrice不能为空");
        verify(() -> order.getAddress() == null, "address不能为空");

        order.setExpressCompany(null);
        order.setExpressId(null);
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        orderMapper.insert(order);
    }

    private void verify(BooleanSupplier supplier, String message) {
        if (supplier.getAsBoolean()) {
            throw new IllegalArgumentException(message);
        }
    }
}
