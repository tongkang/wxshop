package com.tongkang.wxshop.dao;

import com.tongkang.api.data.GoodsInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsStockMapper {
    int deductStock(GoodsInfo goodsInfo);
}
