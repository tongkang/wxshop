package com.tongkang.wxshop.dao;

import com.tongkang.wxshop.entity.DataStatus;
import com.tongkang.wxshop.generator.Goods;
import com.tongkang.wxshop.generator.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsDao {
    private final GoodsMapper goodsMapper;


    public GoodsDao(GoodsMapper goodsMapper) {
        this.goodsMapper = goodsMapper;
    }

    public Goods insertGoods(Goods goods) {
        long id = goodsMapper.insert(goods);
        goods.setId(id);
        return goods;
    }

    public Goods deleteGoodsById(Long goodsId) {
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods == null) {
            throw new ResourceNotFoundException("商品未找到！");
        }
        goods.setStatus(DataStatus.DELETE_STATUS);
        goodsMapper.updateByPrimaryKey(goods);
        return goods;
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}














