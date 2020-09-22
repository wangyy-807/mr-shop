package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entities.BrandEntity;
import com.baidu.shop.entities.CategoryBrandEntity;
import com.baidu.shop.entities.SpuEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.SpuMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BrandServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/8/31
 * @Version V1.0
 **/
@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpuMapper spuMapper;

    @Override
    public Result<List<BrandEntity>> getBrandByCate(Integer cid) {

        List<BrandEntity> list = brandMapper.getBrandByCateId(cid);

        return this.setResultSuccess(list);
    }

    @Override
    public List<String> getNameById(Integer id) {

        List<String> list = brandMapper.getNameById(id);

        return list;
    }

    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        //分页
        if (ObjectUtil.isNotNull(brandDTO.getPage()) && ObjectUtil.isNotNull(brandDTO.getRows()))
            PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());
        Example example = new Example(BrandEntity.class);

        //排序
        if (StringUtil.isNotEmpty(brandDTO.getSort())) example.setOrderByClause(brandDTO.getOrderByClause());

        //条件查询
        if (StringUtil.isNotEmpty(brandDTO.getName())) example.createCriteria().andLike("name","%" + brandDTO.getName() + "%");
        if (ObjectUtil.isNotNull(brandDTO.getId())) example.createCriteria().andEqualTo("id",brandDTO.getId());
        List<BrandEntity> list = brandMapper.selectByExample(example);

        //返回pageInfo  分页、条件查询、排序结果
        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

        return this.setResultSuccess(pageInfo);
    }

    @Transactional
    @Override
    public Result<JsonObject> saveBrandInfo(BrandDTO brandDTO) {

        //通过GeneratedValue注解返回新增之后的id
        BrandEntity entity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        //获取名称首字母
        entity.setLetter(PinyinUtil.getUpperCase(String.valueOf(entity.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        //新增操作
        brandMapper.insertSelective(entity);

                //批量新增关系表中的数据
        return insertCategoryAndBrand(brandDTO, entity);
    }

    @Transactional
    @Override
    public Result<JsonObject> editBrandInfo(BrandDTO brandDTO) {

        //通过GeneratedValue注解返回新增之后的id
        BrandEntity entity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        //获取名称首字母
        entity.setLetter(PinyinUtil.getUpperCase(String.valueOf(entity.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        //修改操作
        brandMapper.updateByPrimaryKeySelective(entity);

        //删除关系表中brandId的数据
        deleteBrandAndCategory(entity.getId());

                //批量新增关系表的数据
        return insertCategoryAndBrand(brandDTO, entity);
    }

    @Transactional
    @Override
    public Result<JsonObject> deleteBrandInfo(Integer brandId) {

        //如果商品绑定了品牌不能删除
        Example example = new Example(SpuEntity.class);
        if (ObjectUtil.isNotNull(brandId))
            example.createCriteria().andEqualTo("brandId",brandId);
        List<SpuEntity> list = spuMapper.selectByExample(example);
        if (list.size() > 0)
            return this.setResultError("该品牌被商品绑定不能删除");

        //删除操作
        brandMapper.deleteByPrimaryKey(brandId);

        //删除关系表中BrandId的数据
        deleteBrandAndCategory(brandId);

        return this.setResultSuccess();
    }

    //封装的删除关系表中brandId为id的数据
    private void deleteBrandAndCategory(Integer id){
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        categoryBrandMapper.deleteByExample(example);
    }

    //封装的批量新增关系表数据
    private Result<JsonObject> insertCategoryAndBrand(BrandDTO brandDTO, BrandEntity entity) {
        if (brandDTO.getCategories().contains(",")){
            List<CategoryBrandEntity> list = Arrays.asList(brandDTO.getCategories().split(",")).stream().map(cid -> {
                CategoryBrandEntity brandEntity = new CategoryBrandEntity();
                brandEntity.setCategoryId(StringUtil.toInteger(cid));
                brandEntity.setBrandId(entity.getId());
                return brandEntity;
            }).collect(Collectors.toList());
            categoryBrandMapper.insertList(list);
        }else{
            CategoryBrandEntity brandEntity = new CategoryBrandEntity();
            brandEntity.setCategoryId(StringUtil.toInteger(brandDTO.getCategories()));
            brandEntity.setBrandId(entity.getId());
            categoryBrandMapper.insertSelective(brandEntity);
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<List<BrandEntity>> getBrandByIdList(String brandIdsStr) {

        List<String> asList = Arrays.asList(brandIdsStr.split(","));
        List<BrandEntity> list = brandMapper.selectByIdList(asList.stream().map(cidStr -> Integer.parseInt(cidStr)).collect(Collectors.toList()));

        return this.setResultSuccess(list);
    }
}
