package com.tongkang.wxshop.service;

import com.tongkang.api.DataStatus;
import com.tongkang.api.data.GoodsInfo;
import com.tongkang.api.data.OrderInfo;
import com.tongkang.api.generator.Order;
import com.tongkang.api.rpc.OrderRpcService;
import com.tongkang.wxshop.dao.GoodsStockMapper;
import com.tongkang.wxshop.entity.HttpException;
import com.tongkang.wxshop.entity.OrderResponse;
import com.tongkang.wxshop.generator.Goods;
import com.tongkang.wxshop.generator.ShopMapper;
import com.tongkang.wxshop.generator.UserMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    @Reference(version = "${wxshop.orderservice.version}")
    private OrderRpcService orderRpcService;

    private UserMapper userMapper;

    private GoodsStockMapper goodsStockMapper;

    private GoodsService goodsService;

    private ShopMapper shopMapper;

    @Autowired
    public OrderService(UserMapper userMapper,
                        GoodsStockMapper goodsStockMapper,
                        ShopMapper shopMapper,
                        GoodsService goodsService) {
        this.userMapper = userMapper;
        this.shopMapper = shopMapper;
        this.goodsService = goodsService;
        this.goodsStockMapper = goodsStockMapper;
    }

    @Transactional
    public void deductStock(OrderInfo orderInfo) {
        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            if (goodsStockMapper.deductStock(goodsInfo) <= 0) {
                LOGGER.error("扣减库存失败，商品id：" + goodsInfo.getId() + "，数量：" + goodsInfo.getNumber());
                throw HttpException.gone("扣减库存失败！");
            }
        }

    }

    public OrderResponse createOrder(OrderInfo orderInfo, Long userId) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(orderInfo.getGoods());
        createOrderViaRpc(orderInfo, userId, idToGoodsMap);

        return null;
    }

    private Order createOrderViaRpc(OrderInfo orderInfo, Long userId, Map<Long, Goods> idToGoodsMap) {
        Order order = new Order();
        order.setUserId(userId);
        order.setShopId(new ArrayList<>(idToGoodsMap.values()).get(0).getShopId());
        order.setStatus(DataStatus.PENDING.getName());

        String address = orderInfo.getAddress() == null ?
                userMapper.selectByPrimaryKey(userId).getAddress() :
                orderInfo.getAddress();
        order.setAddress(address);
        order.setTotalPrice(caluateTotalPrice(orderInfo, idToGoodsMap));

        return orderRpcService.createOrder(orderInfo,order);

    }

    private Long caluateTotalPrice(OrderInfo orderInfo, Map<Long, Goods> idToGoodsMap) {
        long result = 0;

        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            Goods goods = idToGoodsMap.get(goodsInfo.getId());
            if (goods == null) {
                throw HttpException.badRequest("goods id非法：" + goodsInfo.getId());
            }
            if (goodsInfo.getNumber() <= 0) {
                throw HttpException.badRequest("number非法：" + goodsInfo.getNumber());
            }
            result = result + goods.getPrice() * goodsInfo.getNumber();
        }
        return result;
    }

    private Map<Long, Goods> getIdToGoodsMap(List<GoodsInfo> goodsInfo) {
        if (goodsInfo.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> goodsId = goodsInfo
                .stream()
                .map(GoodsInfo::getId)
                .collect(toList());

        return goodsService.getIdToGoodsMap(goodsId);
    }
}
