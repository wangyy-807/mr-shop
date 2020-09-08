package com.baidu.shop.mapper;

import com.baidu.shop.base.Result;
import com.baidu.shop.entities.BrandEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @ClassName BrandMapper
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/8/31
 * @Version V1.0
 **/
public interface BrandMapper extends Mapper<BrandEntity> {

    @Select(value = "select name from tb_brand where id = #{brandId}")
    List<String> getNameById(Integer brandId);

    @Select(value = "select * from tb_brand where id in (select brand_id from tb_category_brand where category_id = #{cid})")
    List<BrandEntity> getBrandByCateId(Integer cid);
}
