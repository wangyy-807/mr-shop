package com.baidu.shop.mapper;

import com.baidu.shop.entities.CategoryEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @ClassName CategoryMapper
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/8/27
 * @Version V1.0
 **/
public interface CategoryMapper extends Mapper<CategoryEntity>, SelectByIdListMapper<CategoryEntity,Integer> {

    @Select(value = "select c.id,c.name from tb_category c where c.id in (select cb.category_id from tb_category_brand cb where cb.brand_id=#{brandId})")
    List<CategoryEntity> getByBrandId(Integer brandId);

    @Select(value = "select GROUP_CONCAT(name SEPARATOR \"/\") from tb_category where id in (#{id1},#{id2},#{id3})")
    String getNameById(Integer id1,Integer id2,Integer id3);
}
