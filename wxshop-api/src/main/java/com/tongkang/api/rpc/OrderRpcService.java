package com.tongkang.api.rpc;

import com.tongkang.api.data.OrderInfo;
import com.tongkang.api.generator.Order;

public interface OrderRpcService {
    Order createOrder(OrderInfo orderInfo, Order order);
}
