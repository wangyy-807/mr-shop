package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.entities.CategoryEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

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

}
