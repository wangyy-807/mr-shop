package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.component.MrRabbitMQ;
import com.baidu.shop.constant.MqMessageConstant;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entities.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private StockMapper stockMapper;

    @Resource
    private MrRabbitMQ mrRabbitMQ;

    @Transactional
    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {

        //分页
        if (ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        //构建条件查询
        Example example = new Example(SpuEntity.class);
        //构建查询条件
        Example.Criteria criteria = example.createCriteria();
        if (StringUtil.isNotEmpty(spuDTO.getTitle()))
            criteria.andLike("title","%" + spuDTO.getTitle() + "%");
        if (ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2)
            criteria.andEqualTo("saleable",spuDTO.getSaleable());
        if (ObjectUtil.isNotNull(spuDTO.getId()))
            criteria.andEqualTo("id",spuDTO.getId());

        //排序
        if (StringUtil.isNotEmpty(spuDTO.getSort()))
            example.setOrderByClause(spuDTO.getOrderByClause());

        //条件查询
        List<SpuEntity> list = spuMapper.selectByExample(example);

        List<SpuDTO> list2 = list.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);
            //调用mapper中的方法查出当前spu中随对应的brandName和categoryName
            SpuDTO dto = spuMapper.getInfo(spuEntity.getId());
            spuDTO1.setCategoryName(dto.getCategoryName());
            spuDTO1.setBrandName(dto.getBrandName());
            return spuDTO1;
        }).collect(Collectors.toList());

        //分页数据
        PageInfo<SpuEntity> info = new PageInfo<>(list);

        return this.setResult(HttpStatus.OK.value(), info.getTotal() + "",list2);
    }

    @Override
    public Result<JsonObject> saveSpuInfo(SpuDTO spuDTO) {

        mrRabbitMQ.send(saveInfoTransaction(spuDTO) + "", MqMessageConstant.SPU_ROUT_KEY_SAVE);

        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> editSpuInfo(SpuDTO spuDTO) {

        editInfoTransaction(spuDTO);

        mrRabbitMQ.send( spuDTO.getId() + "", MqMessageConstant.SPU_ROUT_KEY_UPDATE);
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delSpuInfo(Integer spuId) {

        delInfoTransaction(spuId);

        mrRabbitMQ.send( spuId + "", MqMessageConstant.SPU_ROUT_KEY_DELETE);

        return this.setResultSuccess();
    }

    @Override
    public Result<SpuDetailEntity> getDetailBySpuId(Integer spuId) {

        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);

        return this.setResultSuccess(spuDetailEntity);
    }

    @Override
    public Result<List<SkuDTO>> getSkuBySpuId(Integer spuId) {

        List<SkuDTO> list = skuMapper.selectSkuAndStockBySpuId(spuId);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JsonObject> upperAndLowerShelves(SpuDTO spuDTO) {
        SpuEntity spuEntity = new SpuEntity();
        spuEntity.setId(spuDTO.getId());
        if (spuDTO.getSaleable() == 1){
            spuEntity.setSaleable(0);
        }else{
            spuEntity.setSaleable(1);
        }
        spuMapper.updateByPrimaryKeySelective(spuEntity);

        return this.setResultSuccess();
    }

    @Transactional
    public void delInfoTransaction(Integer spuId) {
        spuMapper.deleteByPrimaryKey(spuId);
        spuDetailMapper.deleteByPrimaryKey(spuId);
        delSkuAndStock(spuId);
    }

    @Transactional
    public void editInfoTransaction(SpuDTO spuDTO) {
        Date date = new Date();
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);
        spuDetailMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(),SpuDetailEntity.class));
        delSkuAndStock(spuDTO.getId());
        this.addSkuAndStock(spuDTO.getSkus(), spuDTO.getId(),date);
    }

    @Transactional
    public Integer saveInfoTransaction(SpuDTO spuDTO) {
        Date date = new Date();

        //新增spu
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        spuMapper.insertSelective(spuEntity);
        Integer spuId = spuEntity.getId();

        //新增spudetail
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuId);
        spuDetailMapper.insertSelective(spuDetailEntity);

        this.addSkuAndStock(spuDTO.getSkus(),spuId,date);
        return spuEntity.getId();
    }

    private void addSkuAndStock(List<SkuDTO> skus, Integer id, Date date) {
        skus.stream().forEach(skuDTO -> {
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(id);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);
        });
    }

    private void delSkuAndStock(Integer id) {
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId", id);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        List<Long> list = skuEntities.stream().map(sku -> sku.getId()).collect(Collectors.toList());
        if (list.size() > 0) {
            skuMapper.deleteByIdList(list);
            stockMapper.deleteByIdList(list);
        }
    }

}
