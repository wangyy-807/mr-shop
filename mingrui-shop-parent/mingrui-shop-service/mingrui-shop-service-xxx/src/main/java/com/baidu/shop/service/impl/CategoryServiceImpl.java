package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entities.CategoryEntity;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.service.CategoryService;
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

        CategoryEntity entity = categoryMapper.selectByPrimaryKey(id);
        if (entity == null) {
            return this.setResultError("传入的id查询不到结果，无效");
        }

        if (entity.getIsParent() == 1){
            return this.setResultError("这是一个父节点不能删除");
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
}
