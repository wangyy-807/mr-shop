package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entities.SpuEntity;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @ClassName GoodsService
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/7
 * @Version V1.0
 **/
@Api(value = "商品接口")
public interface GoodsService {

    @ApiOperation(value = "获取spu数据")
    @GetMapping(value = "goods/getSpuInfo")
    Result<PageInfo<SpuEntity>> getSpuInfo(SpuDTO spuDTO);

    @ApiOperation(value = "新增商品")
    @PostMapping(value = "goods/saveSpuInfo")
    Result<JsonObject> saveSpuInfo(@RequestBody SpuDTO spuDTO);
}
