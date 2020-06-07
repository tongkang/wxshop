package com.tongkang.order.mapper;

import com.tongkang.api.data.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyOrderMapper {

    void insertOrders(OrderInfo orderInfo);

}
