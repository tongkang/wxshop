package com.tongkang.wxshop.service;

import com.tongkang.wxshop.controller.ShoppingCartController;
import com.tongkang.wxshop.dao.ShoppingCartQueryMapper;
import com.tongkang.api.DataStatus;
import com.tongkang.wxshop.entity.HttpException;
import com.tongkang.wxshop.entity.PageResponse;
import com.tongkang.wxshop.entity.ShoppingCartData;
import com.tongkang.wxshop.entity.ShoppingCartGoods;
import com.tongkang.wxshop.generator.Goods;
import com.tongkang.wxshop.generator.GoodsExample;
import com.tongkang.wxshop.generator.GoodsMapper;
import com.tongkang.wxshop.generator.ShoppingCart;
import com.tongkang.wxshop.generator.ShoppingCartMapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.*;

@Service
public class ShoppingCartService {

    private static Logger logger = LoggerFactory.getLogger(ShoppingCartService.class);
    private ShoppingCartQueryMapper shoppingCartQueryMapper;
    private GoodsMapper goodsMapper;
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    public ShoppingCartService(ShoppingCartQueryMapper shoppingCartQueryMapper, GoodsMapper goodsMapper, SqlSessionFactory sqlSessionFactory) {
        this.shoppingCartQueryMapper = shoppingCartQueryMapper;
        this.goodsMapper = goodsMapper;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public PageResponse<ShoppingCartData> getShoppingCartOfUser(Long userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        int totalNum = shoppingCartQueryMapper.countHowManyShopsInUserShoppingCart(userId);
        List<ShoppingCartData> pagedData = shoppingCartQueryMapper.selectShoppingCartDataByUserId(userId, pageSize, offset)
                .stream()
                .collect(groupingBy(shoppingCartData -> shoppingCartData.getShop().getId()))
                .values()
                .stream()
                .map(this::merge)
                .collect(toList());
        int totalPage = totalNum % pageSize == 0 ? totalNum / pageSize : totalNum / pageSize + 1;
        return PageResponse.pagedData(pageNum, pageSize, totalPage, pagedData);
    }

    public ShoppingCartData addToShoppingCart(ShoppingCartController.AddToShoppingCartRequest request) {
        List<Long> goodsId = request.getGoods()
                .stream()
                .map(ShoppingCartController.AddToShoppingCartItem::getId)
                .collect(toList());


        if (goodsId.isEmpty()) {
            throw HttpException.badRequest("商品ID为空！");
        }

        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdIn(goodsId);
        List<Goods> goods = goodsMapper.selectByExample(example);

        if (goods.stream().map(Goods::getShopId).collect(toSet()).size() != 1) {
            logger.debug("非法请求：{}, {}", goodsId, goods);
            throw HttpException.badRequest("商品ID非法！");
        }

        Map<Long, Goods> idToGoodsMap = goods.stream().collect(toMap(Goods::getId, x -> x));

        List<ShoppingCart> shoppingCartRows = request.getGoods()
                .stream()
                .map(item -> toShoppingCartRow(item, idToGoodsMap))
                .filter(Objects::nonNull)
                .collect(toList());

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            ShoppingCartMapper mapper = sqlSession.getMapper(ShoppingCartMapper.class);
            shoppingCartRows.forEach(mapper::insert);
            sqlSession.commit();
        }

        return merge(shoppingCartQueryMapper.selectShoppingCartDataByUserIdShopId(
                UserContext.getCurrentUser().getId(),
                goods.get(0).getShopId()
        ));

    }

    private ShoppingCartData merge(List<ShoppingCartData> goodsOfSameShop) {
        ShoppingCartData result = new ShoppingCartData();
        result.setShop(goodsOfSameShop.get(0).getShop());
        List<ShoppingCartGoods> goods = goodsOfSameShop.stream()
                .map(ShoppingCartData::getGoods)
                .flatMap(List::stream)
                .collect(toList());
        result.setGoods(goods);
        return result;
    }

    private ShoppingCart toShoppingCartRow(ShoppingCartController.AddToShoppingCartItem item,
                                           Map<Long, Goods> idToGoodsMap) {

        Goods goods = idToGoodsMap.get(item.getId());
        if (goods == null) {
            return null;
        }

        ShoppingCart result = new ShoppingCart();
        result.setGoodsId(item.getId());
        result.setNumber(item.getNumber());
        result.setUserId(UserContext.getCurrentUser().getId());
        result.setShopId(goods.getShopId());
        result.setStatus(DataStatus.OK.toString().toLowerCase());
        result.setCreatedAt(new Date());
        result.setUpdatedAt(new Date());
        return result;
    }
}
