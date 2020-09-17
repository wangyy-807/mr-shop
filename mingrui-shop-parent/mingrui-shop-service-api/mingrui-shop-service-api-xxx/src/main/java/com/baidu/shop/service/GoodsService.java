package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entities.SkuEntity;
import com.baidu.shop.entities.SpuDetailEntity;
import com.baidu.shop.entities.SpuEntity;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

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
    Result<List<SpuDTO>> getSpuInfo(@SpringQueryMap SpuDTO spuDTO);

    @ApiOperation(value = "新增商品")
    @PostMapping(value = "goods/saveSpuInfo")
    Result<JsonObject> saveSpuInfo(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "新增商品")
    @PutMapping(value = "goods/saveSpuInfo")
    Result<JsonObject> editSpuInfo(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "通过spuId获取detail数据")
    @GetMapping(value = "goods/getDetailBySpuId")
    Result<SpuDetailEntity> getDetailBySpuId(@RequestParam Integer spuId);

    @ApiOperation(value = "通过spuId获取sku数据")
    @GetMapping(value = "goods/getSkuBySpuId")
    Result<List<SkuDTO>> getSkuBySpuId(@RequestParam Integer spuId);

    @ApiOperation(value = "删除商品")
    @DeleteMapping(value = "goods/delSpuInfo")
    Result<JSONObject> delSpuInfo(Integer spuId);

    @ApiOperation(value = "上下架商品")
    @PutMapping(value = "goods/upperAndLowerShelves")
    Result<JsonObject> upperAndLowerShelves(@RequestBody SpuDTO spuDTO);
}
