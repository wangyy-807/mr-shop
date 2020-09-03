package com.baidu.shop.entities;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName SpecGroupEntity
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/3
 * @Version V1.0
 **/
@Table(name = "tb_spec_group")
@Data
public class SpecGroupEntity {

    @Id
    private Integer id;

    private Integer cid;

    private String name;
}
