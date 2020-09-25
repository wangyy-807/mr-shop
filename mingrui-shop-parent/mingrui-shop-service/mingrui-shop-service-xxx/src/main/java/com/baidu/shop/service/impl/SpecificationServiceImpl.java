package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entities.SpecGroupEntity;
import com.baidu.shop.entities.SpecParamEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.mapper.SpecParamMapper;
import com.baidu.shop.service.SpecificationService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName SpecGroupServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/3
 * @Version V1.0
 **/
@RestController
public class SpecificationServiceImpl extends BaseApiService implements SpecificationService {

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private SpecParamMapper specParamMapper;

    @Override
    public Result<List<SpecGroupEntity>> getSpecGroup(SpecGroupDTO specGroupDTO) {

        Example example = new Example(SpecGroupEntity.class);
        if (ObjectUtil.isNotNull(specGroupDTO.getCid())) example.createCriteria().andEqualTo("cid",specGroupDTO.getCid());

        List<SpecGroupEntity> list = specGroupMapper.selectByExample(example);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JsonObject> saveSpecGroup(SpecGroupDTO specGroupDTO) {
        specGroupMapper.insertSelective(BaiduBeanUtil.copyProperties(specGroupDTO,SpecGroupEntity.class));
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> editSpecGroup(SpecGroupDTO specGroupDTO) {
        specGroupMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specGroupDTO,SpecGroupEntity.class));
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> deleteSpecGroup(Integer id) {

        Example example = new Example(SpecParamEntity.class);
        example.createCriteria().andEqualTo("groupId",id);
        List<SpecParamEntity> list = specParamMapper.selectByExample(example);

        if (list.size() == 0){

            specGroupMapper.deleteByPrimaryKey(id);

        }else{
            SpecGroupEntity entity = specGroupMapper.selectByPrimaryKey(id);

            String paramName = "";
            for (SpecParamEntity entity1 : list) paramName += entity1.getName();

            return this.setResultError("{" + entity.getName() + "}规格组绑定了{" + paramName + "}规格参数，不能删除");
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<List<SpecParamEntity>> getSpecParam(SpecParamDTO specParamDTO) {

        //if (ObjectUtil.isNull(specParamDTO.getGroupId())) return this.setResultError("规格组id为空");

        Example example = new Example(SpecParamEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if (ObjectUtil.isNotNull(specParamDTO.getGroupId()))
            criteria.andEqualTo("groupId",specParamDTO.getGroupId());

        if(ObjectUtil.isNotNull(specParamDTO.getCid()))
            criteria.andEqualTo("cid",specParamDTO.getCid());

        if(ObjectUtil.isNotNull(specParamDTO.getSearching()))
            criteria.andEqualTo("searching",specParamDTO.getSearching());

        if(ObjectUtil.isNotNull(specParamDTO.getGeneric()))
            criteria.andEqualTo("generic",specParamDTO.getGeneric());

        List<SpecParamEntity> list = specParamMapper.selectByExample(example);

        return this.setResultSuccess(list);
    }

    @Override
    public Result<JsonObject> saveSpecParam(SpecParamDTO specParamDTO) {

        specParamMapper.insertSelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));

        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> editSpecParam(SpecParamDTO specParamDTO) {

        specParamMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));

        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> deleteSpecParam(Integer id) {

        specParamMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }
}
