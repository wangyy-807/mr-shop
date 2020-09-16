package com.baidu.shop.mapper;

import com.baidu.shop.entities.SkuEntity;
import com.baidu.shop.entities.StockEntity;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface StockMapper extends Mapper<StockEntity>, DeleteByIdListMapper<StockEntity, Long> {
}
