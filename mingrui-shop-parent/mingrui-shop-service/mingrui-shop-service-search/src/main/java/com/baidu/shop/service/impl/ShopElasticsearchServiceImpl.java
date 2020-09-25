package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entities.BrandEntity;
import com.baidu.shop.entities.CategoryEntity;
import com.baidu.shop.entities.SpecParamEntity;
import com.baidu.shop.entities.SpuDetailEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import feign.Feign;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
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
    private BrandFeign brandFeign;

    @Resource
    private CategoryFeign categoryFeign;

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
            indexOperations.createMapping();
        }

        List<GoodsDoc> goodsDocs = this.esGoodsInfo();
        elasticsearchRestTemplate.save(goodsDocs);

        return this.setResultSuccess();
    }

    @Override
    public GoodsResponse search(String search,Integer page, String filter, Integer cid) {

        if (StringUtil.isEmpty(search)) throw new RuntimeException("查询内容不能为空");

        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(
                this.getNativeSearchQueryBuilder(search, page, filter).build(), GoodsDoc.class);
        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());
        List<GoodsDoc> goodsDocs = highLightHit.stream().map(
                searchHit -> searchHit.getContent())
                .collect(Collectors.toList());

        Aggregations aggregations = searchHits.getAggregations();
        List<CategoryEntity> categoryList = null;
        Integer hotCid = 0;
        Map<Integer, List<CategoryEntity>> map = getCategoryList(aggregations);
        for(Map.Entry<Integer, List<CategoryEntity>> mapEntry : map.entrySet()){
            hotCid = mapEntry.getKey();
            categoryList = mapEntry.getValue();
        }
        List<BrandEntity> brandList = getBrandList(aggregations);

        Map<String, List<String>> map1 = getSpecParam(search, hotCid);

        if (cid == 0) cid = hotCid;
        String data = categoryFeign.getCateName(cid);

        GoodsResponse goodsResponse = new GoodsResponse(searchHits.getTotalHits(),
                Double.valueOf(Math.ceil(Long.valueOf(searchHits.getTotalHits()).doubleValue() / 10)).longValue(),
                brandList, categoryList, goodsDocs, map1, data);

        return goodsResponse;
    }

    private Map<String, List<String>> getSpecParam(String search, Integer hotCid) {
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(1);
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParam(specParamDTO);
        if (specParamResult.getCode() == HttpStatus.OK.value()){
            List<SpecParamEntity> paramData = specParamResult.getData();

            NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
            searchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(
                    search,"title","brandName","categoryName"));
            searchQueryBuilder.withPageable(PageRequest.of(0,1));
            paramData.stream().forEach(specParam -> searchQueryBuilder.addAggregation(
                    AggregationBuilders.terms(specParam.getName()).field("specs." + specParam.getName() + ".keyword")));

            SearchHits<GoodsDoc> search1 = elasticsearchRestTemplate.search(searchQueryBuilder.build(), GoodsDoc.class);
            HashMap<String, List<String>> map1 = new HashMap<>();
            Aggregations aggregations1 = search1.getAggregations();
            paramData.stream().forEach(specParam -> {
                Terms terms = aggregations1.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                List<String> valueList = buckets.stream().map(
                        bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
                map1.put(specParam.getName(),valueList);
            });
            return map1;
        }
        return null;
    }

    private NativeSearchQueryBuilder getNativeSearchQueryBuilder(String search, Integer page, String filter) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();

        if (StringUtil.isNotEmpty(filter) && filter.length() > 2){
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            Map<String, String> filterMap = JSONUtil.toMapValueString(filter);
            filterMap.forEach((key,value) -> {
                MatchQueryBuilder matchQueryBuilder =null;
                if (key.equals("cid3") || key.equals("brandId")){
                    matchQueryBuilder = QueryBuilders.matchQuery(key, value);
                }else{
                    matchQueryBuilder = QueryBuilders.matchQuery("specs." + key + ".keyword",value);
                }
                boolQuery.must(matchQueryBuilder);
            });
            searchQueryBuilder.withFilter(boolQuery);
        }

        searchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(
                search,"title","brandName","categoryName"));

        searchQueryBuilder.addAggregation(AggregationBuilders.terms("cid_agg").field("cid3"));
        searchQueryBuilder.addAggregation(AggregationBuilders.terms("brand_agg").field("brandId").size(14));

        searchQueryBuilder.withPageable(PageRequest.of(page -1,10));
        searchQueryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));
        return searchQueryBuilder;
    }

    private Map<Integer, List<CategoryEntity>> getCategoryList(Aggregations aggregations) {
        Terms cid_agg = aggregations.get("cid_agg");
        List<? extends Terms.Bucket> cidBuckets = cid_agg.getBuckets();
        List<Integer> hotCid = Arrays.asList(0);
        List<Long> maxCount = Arrays.asList(0L);
        Map<Integer, List<CategoryEntity>> map = new HashMap<>();
        List<String> cidList = cidBuckets.stream().map(cidBucket -> {

            if (cidBucket.getDocCount() > maxCount.get(0)){
                maxCount.set(0,cidBucket.getDocCount());
                hotCid.set(0,cidBucket.getKeyAsNumber().intValue());
            }

            return cidBucket.getKeyAsString();
        }).collect(Collectors.toList());
        Result<List<CategoryEntity>> categoryResult = categoryFeign.getCategoryByIdList(
                String.join(",",cidList));

        map.put(hotCid.get(0),categoryResult.getData());

        return map;
    }

    private List<BrandEntity> getBrandList(Aggregations aggregations) {
        Terms brand_agg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> brandIdBuckets = brand_agg.getBuckets();
        List<String> brandIdList = brandIdBuckets.stream().map(
                brandIdBucket -> brandIdBucket.getKeyAsString())
                .collect(Collectors.toList());
        Result<List<BrandEntity>> brandResult = brandFeign.getBrandByIdList(String.join(",",brandIdList));
        return brandResult.getData();
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
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        if (skuBySpuId.getCode() == HttpStatus.OK.value()){
            List<SkuDTO> skuList = skuBySpuId.getData();
            skuMapList = skuList.stream().map(sku -> {
                Map<String, Object> skuMap = new HashMap<>();
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

    private Map<String, Object> getSpecs(SpuDTO spu){
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
                            specMap.put(specParam.getName(),
                                    chooseSegment(genericMap.get(specParam.getId() + ""),
                                    specParam.getSegments(),
                                    specParam.getUnit()));
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
