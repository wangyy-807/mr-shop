package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entities.CategoryEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName CategoryService
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/8/27
 * @Version V1.0
 **/
@Api(tags = "商品分类接口")
public interface CategoryService {

    @ApiOperation(value = "通过查询商品分类")
    @GetMapping(value = "category/list")
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid);

    @ApiOperation(value = "通过查询商品分类")
    @PostMapping(value = "category/save")
    public Result<JSONObject> addCategory(@RequestBody CategoryEntity entity);

    @ApiOperation(value = "通过查询商品分类")
    @PutMapping(value = "category/edit")
    public Result<JSONObject> editCategory(@RequestBody CategoryEntity entity);

    @ApiOperation(value = "通过查询商品分类")
    @DeleteMapping(value = "category/delete")
    public Result<JSONObject> delCategory(Integer id);

    @ApiOperation(value = "通过品牌id查询商品分类")
    @GetMapping(value = "category/getByBrand")
    public Result<List<CategoryEntity>> getByBrandId(Integer brandId);

    @ApiOperation(value = "通过分类id集合查询商品分类")
    @GetMapping(value = "category/getCategoryByIdList")
    Result<List<CategoryEntity>> getCategoryByIdList(@RequestParam String cidStr);
}
