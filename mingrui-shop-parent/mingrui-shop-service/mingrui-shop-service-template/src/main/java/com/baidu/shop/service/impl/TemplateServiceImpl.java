package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entities.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TemplateServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/25
 * @Version V1.0
 **/
@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Autowired
    private BrandFeign brandFeign;

    //注入静态化模版
    @Autowired
    private TemplateEngine templateEngine;

    //静态文件生成的路径
    @Value(value = "${mrshop.static.html.path}")
    private String staticHTMLPath;


    @Override
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {

        Map<String, Object> map = getStringObjectMap(spuId);

        //创建模板引擎上下文
        Context context = new Context();
        //将所有准备的数据放到模板中
        context.setVariables(map);

        //创建文件 param1:文件路径 param2:文件名称
        File file = new File(staticHTMLPath, spuId + ".html");
        //构建文件输出流
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            //根据模板生成静态文件
            //param1:模板名称 params2:模板上下文[上下文中包含了需要填充的数据],文件输出流
            templateEngine.process("item",context,writer);
            writer.close();
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initStaticHTMLTemplate() {

        //获取所有的spu信息,注意:应该写一个只获取id集合的接口,我只是为了省事
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(new SpuDTO());
        if(spuInfo.getCode() == 200){

            List<SpuDTO> spuList = spuInfo.getData();

            spuList.stream().forEach(spu -> {
                this.createStaticHTMLTemplate(spu.getId());
            });
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delHTMLBySpuId(Integer spuId) {

        File file = new File(staticHTMLPath + File.separator + spuId + ".html");

        if(!file.delete()){
            return this.setResultError("文件删除失败");
        }

        return this.setResultSuccess();
    }

    private Map<String, Object> getStringObjectMap(Integer spuId) {
        Map<String, Object> map = new HashMap<>();
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(spuDTO);
        if (spuInfoResult.getCode() == HttpStatus.SC_OK){
            List<SpuDTO> data = spuInfoResult.getData();
            if (data.size() == 1){
                //spu信息
                SpuDTO spuInfo = data.get(0);
                map.put("spuInfo",spuInfo);
                //品牌信息
                List<BrandEntity> brandList = getBrandList(spuInfo);
                map.put("brandInfo",brandList.get(0));
                //分类信息
                List<CategoryEntity> cateList = getCateList(spuInfo);
                map.put("categoryList", cateList);
                //特有规格信息
                Map<Integer, String> specMap = getSpecMap(spuInfo);
                map.put("specParamMap",specMap);
                //sku信息
                List<SkuDTO> skuList = getSkuList(spuInfo);
                map.put("skus",skuList);
                //spuDetail信息
                SpuDetailEntity spuDetailInfo = getSpuDetailInfo(spuInfo.getId());
                map.put("spuDetailInfo",spuDetailInfo);
                //规格组信息
                List<SpecGroupDTO> groupSpec = getGroupSpec(spuInfo);
                map.put("groupSpec",groupSpec);
            }
        }
        return map;
    }
    
    private List<BrandEntity> getBrandList(SpuDTO spuInfo){
        BrandDTO brandDTO = new BrandDTO();
        brandDTO.setId(spuInfo.getBrandId());
        Result<PageInfo<BrandEntity>> brandInfoResult = brandFeign.getBrandInfo(brandDTO);
        if (brandInfoResult.getCode() == HttpStatus.SC_OK){
            List<BrandEntity> brandList = brandInfoResult.getData().getList();
            if (brandList.size() == 1){
                return brandList;
            }
        }
        return null;
    }

    private List<CategoryEntity> getCateList(SpuDTO spuInfo){
        Result<List<CategoryEntity>> categoryByIdList = categoryFeign.getCategoryByIdList(String.join(",", Arrays.asList(spuInfo.getCid1() + "", spuInfo.getCid2() + "", spuInfo.getCid3() + "")));
        if (categoryByIdList.getCode() == HttpStatus.SC_OK){
            List<CategoryEntity> cateList = categoryByIdList.getData();
            return cateList;
        }
        return null;
    }

    private Map<Integer, String> getSpecMap(SpuDTO spuInfo){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuInfo.getCid3());
        specParamDTO.setGeneric(0);
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParam(specParamDTO);
        if (specParamResult.getCode() == HttpStatus.SC_OK){
            List<SpecParamEntity> specParamList = specParamResult.getData();
            Map<Integer, String> specMap = new HashMap<>();
            specParamList.stream().forEach(specParamEntity -> specMap.put(specParamEntity.getId(),specParamEntity.getName()));
            return specMap;
        }
        return null;
    }

    private List<SkuDTO> getSkuList(SpuDTO spuInfo){
        Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuInfo.getId());
        if (skusResult.getCode() == HttpStatus.SC_OK){
            List<SkuDTO> skusList = skusResult.getData();
            return skusList;
        }
        return null;
    }

    private SpuDetailEntity getSpuDetailInfo(Integer spuId){
        Result<SpuDetailEntity> spuDetailResult = goodsFeign.getDetailBySpuId(spuId);
        if(spuDetailResult.getCode() == 200){
            SpuDetailEntity spuDetailInfo = spuDetailResult.getData();
            return spuDetailInfo;
        }
        return null;
    }

    private List<SpecGroupDTO> getGroupSpec(SpuDTO spuInfo){
        SpecGroupDTO specGroupDTO = new SpecGroupDTO();
        specGroupDTO.setCid(spuInfo.getCid3());
        Result<List<SpecGroupEntity>> specGroupResult = specificationFeign.getSpecGroup(specGroupDTO);
        if (specGroupResult.getCode() == HttpStatus.SC_OK){
            List<SpecGroupEntity> specGroupList = specGroupResult.getData();
            List<SpecGroupDTO> groupSpec = specGroupList.stream().map(specGroupEntity -> {
                SpecGroupDTO groupDTO = BaiduBeanUtil.copyProperties(specGroupEntity, SpecGroupDTO.class);
                //通用规格参数
                SpecParamDTO paramDTO = new SpecParamDTO();
                paramDTO.setGroupId(specGroupEntity.getId());
                paramDTO.setGeneric(1);
                Result<List<SpecParamEntity>> paramResult = specificationFeign.getSpecParam(paramDTO);
                if (paramResult.getCode() == HttpStatus.SC_OK) {
                    groupDTO.setSpecParamList(paramResult.getData());
                }
                return groupDTO;
            }).collect(Collectors.toList());
            return groupSpec;
        }
        return null;
    }
}
