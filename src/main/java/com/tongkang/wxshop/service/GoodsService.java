package com.tongkang.wxshop.service;

import com.tongkang.wxshop.dao.GoodsDao;
import com.tongkang.wxshop.dao.ShopDao;
import com.tongkang.wxshop.generator.Goods;
import com.tongkang.wxshop.generator.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GoodsService {

    private final GoodsDao goodsDao;
    private final ShopDao shopDao;

    @Autowired
    public GoodsService(GoodsDao goodsDao, ShopDao shopDao) {
        this.goodsDao = goodsDao;
        this.shopDao = shopDao;
    }

    public Goods createGoods(Goods goods) {

//        return goodsDao.insertGoods(goods);

//        要在所在的商店中才能进行创建商品
        Shop shop = shopDao.findShopById(goods.getShopId());

        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            return goodsDao.insertGoods(goods);
        } else {
            throw new NotAuthorizedForShopException("无权访问！");
        }
    }

    public Goods deleteGoodsById(Long goodsId) {
//        return goodsDao.deleteGoodsById(goodsId);

        Shop shop = shopDao.findShopById(goodsId);
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            return goodsDao.deleteGoodsById(goodsId);
        } else {
            throw new NotAuthorizedForShopException("无权访问！");
        }

    }

    public static class NotAuthorizedForShopException extends RuntimeException {
        public NotAuthorizedForShopException(String message) {
            super(message);
        }
    }
}
