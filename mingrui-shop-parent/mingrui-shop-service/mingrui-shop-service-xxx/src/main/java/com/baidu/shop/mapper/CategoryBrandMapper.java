package com.baidu.shop.mapper;

import com.baidu.shop.entities.CategoryBrandEntity;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ClassName CategoryBrandMapper
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/1
 * @Version V1.0
 **/
public interface CategoryBrandMapper extends Mapper<CategoryBrandEntity>, InsertListMapper<CategoryBrandEntity> {
}
