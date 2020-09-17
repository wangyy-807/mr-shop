package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entities.SpecParamEntity;
import com.baidu.shop.entities.SpuDetailEntity;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.utils.JSONUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/16
 * @Version V1.0
 **/
@RestController
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {

    @Resource
    private GoodsFeign goodsFeign;

    @Resource
    private SpecificationFeign specificationFeign;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Result<JSONObject> clearEsData() {

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if (indexOperations.exists()) indexOperations.delete();

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> saveEsData() {

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if (!indexOperations.exists()){

            //indexOperations.create();
            indexOperations.createMapping();

        }

        List<GoodsDoc> goodsDocs = this.esGoodsInfo();
        elasticsearchRestTemplate.save(goodsDocs);

        return this.setResultSuccess();
    }

    private List<GoodsDoc> esGoodsInfo() {

        List<GoodsDoc> goodsDocs = new ArrayList<>();
        SpuDTO spuDTO = new SpuDTO();
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);
        if (spuInfo.getCode() == HttpStatus.OK.value()){
            List<SpuDTO> spuList = spuInfo.getData();
            spuList.stream().forEach(spu -> {

                GoodsDoc goodsDoc = new GoodsDoc();
                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());
                goodsDoc.setSubTitle(spu.getSubTitle());
                goodsDoc.setBrandId(spu.getBrandId().longValue());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setCreateTime(spu.getCreateTime());

                Map<List<Long>, List<Map<String, Object>>> skusAndPrice = getSkusAndPrice(spu.getId());
                skusAndPrice.forEach((key,value) -> {
                    goodsDoc.setPrice(key);
                    goodsDoc.setSkus(JSONUtil.toJsonString(value));
                });

                Map<String, Object> specMap = getSpecs(spu);
                goodsDoc.setSpecs(specMap);
                goodsDocs.add(goodsDoc);
            });
        }
        return goodsDocs;
    }

    private Map<List<Long>,List<Map<String, Object>>> getSkusAndPrice(Integer spuId){
        Map<List<Long>,List<Map<String, Object>>> hashMap = new HashMap<>();

        Result<List<SkuDTO>> skuBySpuId = goodsFeign.getSkuBySpuId(spuId);

        List<Long> priceList = new ArrayList<>();
        Map<String, Object> skuMap = new HashMap<>();
        List<Map<String, Object>> skuMapList = null;
        if (skuBySpuId.getCode() == HttpStatus.OK.value()){
            List<SkuDTO> skuList = skuBySpuId.getData();
            skuMapList = skuList.stream().map(sku -> {
                skuMap.put("id", sku.getId());
                skuMap.put("title", sku.getTitle());
                skuMap.put("images", sku.getImages());
                skuMap.put("price", sku.getPrice());
                priceList.add(sku.getPrice().longValue());
                return skuMap;
            }).collect(Collectors.toList());
        }
        hashMap.put(priceList,skuMapList);
        return hashMap;
    }

    private Map<String, Object> getSpecs(SpuDTO spu) {
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spu.getCid3());
        specParamDTO.setSearching(1);
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParam(specParamDTO);
        Map<String, Object> specMap = new HashMap<>();
        if (specParamResult.getCode() == HttpStatus.OK.value()){
            //里面只有规格参数的ID和名称
            List<SpecParamEntity> specList = specParamResult.getData();
            //通过spuId查找spuDetail，里面有通用参数和特有参数
            Result<SpuDetailEntity> spuDetailResult = goodsFeign.getDetailBySpuId(spu.getId());
            if (spuDetailResult.getCode() == HttpStatus.OK.value()){
                SpuDetailEntity detailInfo = spuDetailResult.getData();
                //通用参数的值
                String genericSpec = detailInfo.getGenericSpec();
                Map<String, String> genericMap = JSONUtil.toMapValueString(genericSpec);
                //特有参数的值
                String specialSpec = detailInfo.getSpecialSpec();
                Map<String, List<String>> specialMap = JSONUtil.toMapValueStrList(specialSpec);
                specList.stream().forEach(specParam -> {
                    if (specParam.getGeneric() == 1){
                        if (specParam.getNumeric() == 1 && specParam.getSearching() == 1){
                            specMap.put(specParam.getName(),chooseSegment(genericMap.get(specParam.getId() + ""),specParam.getSegments(),specParam.getUnit()));
                        }else{
                            specMap.put(specParam.getName(),genericMap.get(specParam.getId() + ""));
                        }
                    }else{
                        specMap.put(specParam.getName(),specialMap.get(specParam.getId() + ""));
                    }
                });
            }
        }
        return specMap;
    }

    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }
}
