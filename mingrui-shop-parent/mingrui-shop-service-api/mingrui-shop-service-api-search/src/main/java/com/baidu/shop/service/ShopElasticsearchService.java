package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @ClassName ShopElasticsearchService
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/16
 * @Version V1.0
 **/
@Api(value = "es接口")
public interface ShopElasticsearchService {

    @ApiOperation(value = "清空ES中的商品数据")
    @GetMapping(value = "es/clearEsData")
    Result<JSONObject> clearEsData();

    @ApiOperation(value = "初始化ES中的商品数据")
    @GetMapping(value = "es/saveEsData")
    Result<JSONObject> saveEsData();

}
