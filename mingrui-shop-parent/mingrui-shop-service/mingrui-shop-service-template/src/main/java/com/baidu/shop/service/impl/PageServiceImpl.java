package com.baidu.shop.service.impl;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entities.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.PageService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/23
 * @Version V1.0
 **/
//@Service
public class PageServiceImpl implements PageService {

    //@Resource
    private GoodsFeign goodsFeign;

    //@Resource
    private BrandFeign brandFeign;

    //@Resource
    private CategoryFeign categoryFeign;

    //@Resource
    private SpecificationFeign specificationFeign;

    @Override
    public Map<String, Object> getGoodsInfo(Integer spuId) {

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
                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo.getBrandId());
                Result<PageInfo<BrandEntity>> brandInfoResult = brandFeign.getBrandInfo(brandDTO);
                if (brandInfoResult.getCode() == HttpStatus.SC_OK){
                    List<BrandEntity> brandList = brandInfoResult.getData().getList();
                    if (brandList.size() == 1){
                        map.put("brandInfo",brandList.get(0));
                    }
                }
                //分类信息
                Result<List<CategoryEntity>> categoryByIdList = categoryFeign.getCategoryByIdList(String.join(",", Arrays.asList(spuInfo.getCid1() + "", spuInfo.getCid2() + "", spuInfo.getCid3() + "")));
                if (categoryByIdList.getCode() == HttpStatus.SC_OK){
                    map.put("categoryList", categoryByIdList.getData());
                }
                //特有规格信息
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spuInfo.getCid3());
                specParamDTO.setGeneric(0);
                Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParam(specParamDTO);
                if (specParamResult.getCode() == HttpStatus.SC_OK){
                    List<SpecParamEntity> specParamList = specParamResult.getData();
                    Map<Integer, String> specMap = new HashMap<>();
                    specParamList.stream().forEach(specParamEntity -> specMap.put(specParamEntity.getId(),specParamEntity.getName()));
                    map.put("specParamMap",specMap);
                }
                //sku信息
                Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuInfo.getId());
                if (skusResult.getCode() == HttpStatus.SC_OK){
                    List<SkuDTO> skusList = skusResult.getData();
                    map.put("skus",skusList);
                }
                //spuDetail信息
                Result<SpuDetailEntity> detailResult = goodsFeign.getDetailBySpuId(spuInfo.getId());
                if (detailResult.getCode() == HttpStatus.SC_OK){
                    SpuDetailEntity spuDetailInfo = detailResult.getData();
                    map.put("spuDetailInfo",spuDetailInfo);
                }
                //规格组信息
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
                    map.put("groupSpec",groupSpec);
                }
            }
        }
        return map;
    }

}
