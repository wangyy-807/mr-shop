package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entities.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.CategoryService;
import com.baidu.shop.utils.ObjectUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/8/27
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private SpuMapper spuMapper;

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> addCategory(CategoryEntity entity) {

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(entity.getParentId());
        categoryEntity.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(categoryEntity);

        categoryMapper.insertSelective(entity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> editCategory(CategoryEntity entity) {

        categoryMapper.updateByPrimaryKeySelective(entity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> delCategory(Integer id) {

        if (ObjectUtil.isNull(id)) return this.setResultError("传入的id查询不到结果，无效");

        CategoryEntity entity = categoryMapper.selectByPrimaryKey(id);
        if (entity.getIsParent() == 1) return this.setResultError("这是一个父节点不能删除");

        Example specGroupExample = new Example(SpecGroupEntity.class);
        specGroupExample.createCriteria().andEqualTo("cid",id);
        List<SpecGroupEntity> specGroupEntities = specGroupMapper.selectByExample(specGroupExample);

        Example categoryBrandExample = new Example(CategoryBrandEntity.class);
        categoryBrandExample.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> categoryBrandEntities = categoryBrandMapper.selectByExample(categoryBrandExample);

        Example example1 = new Example(SpuEntity.class);
        if (ObjectUtil.isNotNull(id))
            example1.createCriteria().andEqualTo("cid3",id);
        List<SpuEntity> spuEntities = spuMapper.selectByExample(example1);

        if (spuEntities.size() != 0)
            return this.setResultError("分类被商品绑定不能删除");

        String msg = "{" + entity.getName() + "}分类";
        if (specGroupEntities.size() != 0){
            msg += "绑定了{" + specGroupEntities.get(0).getName() + "}等规格组，不能删除";
            if (categoryBrandEntities.size() != 0) msg += "同时，被{" + brandMapper.selectByPrimaryKey(categoryBrandEntities.get(0).getBrandId()).getName()+ "," + "}等品牌绑定，不能删除";
            return this.setResultError(msg);
        }
        if (categoryBrandEntities.size() != 0){
            msg += "被{" + brandMapper.selectByPrimaryKey(categoryBrandEntities.get(0).getBrandId()).getName()+ "," + "}等品牌绑定，不能删除";
            return this.setResultError(msg);
        }

        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",entity.getParentId());
        List<CategoryEntity> list = categoryMapper.selectByExample(example);

        if (list.size() == 1){
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setId(entity.getParentId());
            categoryEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(categoryEntity);
        }

        categoryMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<CategoryEntity>> getByBrandId(Integer brandId) {

        List<CategoryEntity> list = categoryMapper.getByBrandId(brandId);

        return this.setResultSuccess(list);
    }
}
