package com.tongkang.wxshop.service;

import com.tongkang.wxshop.entity.DataStatus;
import com.tongkang.wxshop.entity.HttpException;
import com.tongkang.wxshop.entity.PageResponse;
import com.tongkang.wxshop.generator.Goods;
import com.tongkang.wxshop.generator.GoodsMapper;
import com.tongkang.wxshop.generator.Shop;
import com.tongkang.wxshop.generator.ShopMapper;
import com.tongkang.wxshop.generator.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoodsServiceTest {

    @Mock
    private GoodsMapper goodsMapper;
    @Mock
    private ShopMapper shopMapper;
    @Mock
    private Shop shop;
    @Mock
    private Goods goods;

    @InjectMocks
    private GoodsService goodsService;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setId(1L);
        UserContext.setCurrentUser(user);

        lenient().when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
    }

    @AfterEach
    public void clearUserContext() {
        UserContext.setCurrentUser(null);
    }

    @Test
    public void createGoodsSucceedIfUserIsOwner() {
        when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.insert(goods)).thenReturn(123);

        assertEquals(goods, goodsService.createGoods(goods));

        verify(goods).setId(123L);
    }

    @Test
    public void createGoodsFailedIfUserIsNotOwner() {
        when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
        when(shop.getOwnerUserId()).thenReturn(2L);
        HttpException httpException = assertThrows(HttpException.class, () -> {
            goodsService.createGoods(goods);
        });

        assertEquals(403, httpException.getStatusCode());
    }

    @Test
    public void throwExceptionIfGoodsNotFound() {
        long goodsToBeDeleted = 123;

        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.selectByPrimaryKey(goodsToBeDeleted)).thenReturn(null);
        HttpException httpException = assertThrows(HttpException.class, () -> {
            goodsService.deleteGoodsById(goodsToBeDeleted);
        });

        assertEquals(404, httpException.getStatusCode());
    }

    @Test
    public void deleteGoodsThrowExceptionIfUserIsNotOwner() {
        long goodsToBeDeleted = 123;

        when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
        when(shop.getOwnerUserId()).thenReturn(2L);
        HttpException httpException = assertThrows(HttpException.class, () -> {
            goodsService.deleteGoodsById(goodsToBeDeleted);
        });

        assertEquals(403, httpException.getStatusCode());
    }

    @Test
    public void deleteGoodsSucceed() {
        long goodsToBeDeleted = 123;

        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.selectByPrimaryKey(goodsToBeDeleted)).thenReturn(goods);

        goodsService.deleteGoodsById(goodsToBeDeleted);
        verify(goods).setStatus(DataStatus.DELETED.getName());
    }

    @Test
    public void getGoodsSucceedWithNullShopId() {
        int pageNum = 5;
        int pageSize = 10;

        List<Goods> mockData = Mockito.mock(List.class);

        //totalNumber=55
        when(goodsMapper.countByExample(any())).thenReturn(55L);
        when(goodsMapper.selectByExample(any())).thenReturn(mockData);

        PageResponse<Goods> result = goodsService.getGoods(pageNum, pageSize, null);

        assertEquals(6, result.getTotalPage());
        assertEquals(5, result.getPageNum());
        assertEquals(10, result.getPageSize());

        assertEquals(mockData, result.getData());
    }

    @Test
    public void getGoodsSucceedWithNonNullShopId() {
        int pageNum = 5;
        int pageSize = 10;

        List<Goods> mockData = Mockito.mock(List.class);

        when(goodsMapper.countByExample(any())).thenReturn(100L);
        when(goodsMapper.selectByExample(any())).thenReturn(mockData);

        PageResponse<Goods> result = goodsService.getGoods(pageNum, pageSize, 123);

        assertEquals(10, result.getTotalPage());
        assertEquals(5, result.getPageNum());
        assertEquals(10, result.getPageSize());

        assertEquals(mockData, result.getData());
    }

    @Test
    public void updateGoodsSucceed() {
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.updateByExample(any(), any())).thenReturn(1);

        assertEquals(goods, goodsService.updateGoods(goods));
    }

    @Test
    public void updateGoodsNotFound() {
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.updateByExample(any(), any())).thenReturn(0);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            goodsService.updateGoods(goods);
        });

        assertEquals(404, httpException.getStatusCode());
    }

    @Test
    public void updateGoodsFailedIfUserIsNotOwner() {
        when(shop.getOwnerUserId()).thenReturn(2L);

        HttpException httpException = assertThrows(HttpException.class, () -> {
            goodsService.updateGoods(goods);
        });

        assertEquals(403, httpException.getStatusCode());
    }
}
