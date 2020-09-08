package com.baidu.shop.mapper;

import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entities.SpuEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface SpuMapper extends Mapper<SpuEntity> {

    @Select(value = "SELECT s.*,b.`name` AS brandName,GROUP_CONCAT( c.NAME SEPARATOR \"/\" ) AS categoryName FROM tb_brand b,tb_spu s,tb_category c WHERE s.brand_id = b.id AND s.id = #{id} and c.id IN ( s.cid1, s.cid2, s.cid3 )")
    SpuDTO getInfo(Integer id);
}
