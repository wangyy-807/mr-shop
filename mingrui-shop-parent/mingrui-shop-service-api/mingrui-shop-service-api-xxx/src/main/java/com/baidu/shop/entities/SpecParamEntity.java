package com.baidu.shop.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName SpecParamEntity
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/3
 * @Version V1.0
 **/
@Table(name = "tb_spec_param")
@Data
public class SpecParamEntity {

    @Id
    private Integer id;

    private Integer cid;

    private Integer groupId;

    private String name;

    @Column(name = "`numeric`")
    private Integer numeric;

    private String unit;

    private Integer generic;

    private Integer searching;

    private String segments;


}
