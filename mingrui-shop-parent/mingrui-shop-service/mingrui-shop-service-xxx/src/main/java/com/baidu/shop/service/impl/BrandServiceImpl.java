package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entities.BrandEntity;
import com.baidu.shop.entities.CategoryBrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());
        Example example = new Example(BrandEntity.class);

        if (StringUtil.isNotEmpty(brandDTO.getSort())) example.setOrderByClause(brandDTO.getOrderByClause());
        if (StringUtil.isNotEmpty(brandDTO.getName())) example.createCriteria().andLike("name","%" + brandDTO.getName() + "%");

        List<BrandEntity> list = brandMapper.selectByExample(example);

        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

        return this.setResultSuccess(pageInfo);
    }

    @Override
    public Result<JsonObject> saveBrandInfo(BrandDTO brandDTO) {

        BrandEntity entity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        entity.setLetter(PinyinUtil.getUpperCase(String.valueOf(entity.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        brandMapper.insertSelective(entity);

        return insertCategoryAndBrand(brandDTO, entity);
    }

    @Override
    public Result<JsonObject> editBrandInfo(BrandDTO brandDTO) {

        BrandEntity entity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        entity.setLetter(PinyinUtil.getUpperCase(String.valueOf(entity.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        brandMapper.updateByPrimaryKeySelective(entity);

        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",entity.getId());
        categoryBrandMapper.deleteByExample(example);

        return insertCategoryAndBrand(brandDTO, entity);
    }

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
}
