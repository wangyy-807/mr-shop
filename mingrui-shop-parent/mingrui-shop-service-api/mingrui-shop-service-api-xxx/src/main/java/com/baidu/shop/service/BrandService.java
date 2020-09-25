package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entities.BrandEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import java.util.List;

/**
 * @ClassName BrandService
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/8/27
 * @Version V1.0
 **/
@Api(tags = "品牌管理接口")
public interface BrandService {

    @GetMapping(value = "brand/getBrandInfo")
    @ApiOperation(value = "获取品牌信息")
    public Result<PageInfo<BrandEntity>> getBrandInfo(@SpringQueryMap BrandDTO brandDTO);

    @PostMapping(value = "brand/save")
    @ApiOperation(value = "新增品牌信息")
    public Result<JsonObject> saveBrandInfo(@Validated({MingruiOperation.Add.class}) @RequestBody BrandDTO brandDTO);

    @PutMapping(value = "brand/save")
    @ApiOperation(value = "修改品牌信息")
    public Result<JsonObject> editBrandInfo(@Validated({MingruiOperation.Update.class}) @RequestBody BrandDTO brandDTO);

    @DeleteMapping(value = "brand/delete")
    @ApiOperation(value = "删除品牌信息")
    public Result<JsonObject> deleteBrandInfo(Integer brandId);

    @ApiOperation(value = "通过id查询品牌名称")
    @GetMapping(value = "brand/getNameById")
    List<String> getNameById(Integer id);

    @GetMapping(value = "brand/getBrandByCate")
    @ApiOperation(value = "通过分类id获取品牌信息")
    public Result<List<BrandEntity>> getBrandByCate(Integer cid);

    @GetMapping(value = "brand/getBrandByIdList")
    @ApiOperation(value = "通过品牌id集合获取品牌信息")
    Result<List<BrandEntity>> getBrandByIdList(@RequestParam String brandIdsStr);
}
