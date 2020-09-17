package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entities.SpecGroupEntity;
import com.baidu.shop.entities.SpecParamEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName SpecificationService
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/3
 * @Version V1.0
 **/
@Api(value = "规范组接口")
public interface SpecificationService {

    @ApiOperation(value = "查询规格组")
    @GetMapping(value = "specGroup/getInfo")
    Result<List<SpecGroupEntity>> getSpecGroup(SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "新增规格组")
    @PostMapping(value = "specGroup/save")
    Result<JsonObject> saveSpecGroup(@Validated(MingruiOperation.Add.class) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "修改规格组")
    @PutMapping(value = "specGroup/save")
    Result<JsonObject> editSpecGroup(@Validated(MingruiOperation.Update.class) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "删除规格组")
    @DeleteMapping(value = "specGroup/delete")
    Result<JsonObject> deleteSpecGroup(Integer id);

    @ApiOperation(value = "查询规格参数")
    @GetMapping(value = "specParam/getInfo")
    Result<List<SpecParamEntity>> getSpecParam(@SpringQueryMap SpecParamDTO specParamDTO);

    @ApiOperation(value = "新增规格参数")
    @PostMapping(value = "specParam/save")
    Result<JsonObject> saveSpecParam(@Validated(MingruiOperation.Add.class) @RequestBody SpecParamDTO specParamDTO);

    @ApiOperation(value = "修改规格参数")
    @PutMapping(value = "specParam/save")
    Result<JsonObject> editSpecParam(@Validated(MingruiOperation.Update.class) @RequestBody SpecParamDTO specParamDTO);

    @ApiOperation(value = "删除规格参数")
    @DeleteMapping(value = "specParam/delete")
    Result<JsonObject> deleteSpecParam(Integer id);

}
