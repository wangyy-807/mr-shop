package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.UserService;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.LuosimaoDuanxinUtil;
import io.lettuce.core.dynamic.codec.RedisCodecResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/13
 * @Version V1.0
 **/
@RestController
@Slf4j
public class UserServiceImpl extends BaseApiService implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public Result<JSONObject> register(UserDTO userDTO) {

        System.out.println(userDTO);
        UserEntity userEntity = BaiduBeanUtil.copyProperties(userDTO, UserEntity.class);
        userEntity.setCreated(new Date());
        userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));

        userMapper.insertSelective(userEntity);

        return this.setResultSuccess();
    }

    @Override
    public Result<List<UserEntity>> check(String value, Integer type) {

        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if (type == 1){
            criteria.andEqualTo("username",value);
        }else if (type == 2){
            criteria.andEqualTo("phone",value);
        }

        List<UserEntity> userEntities = userMapper.selectByExample(example);

        return this.setResultSuccess(userEntities);
    }

    @Override
    public Result<JSONObject> sendValidCode(UserDTO userDTO) {

        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";

        log.debug("发送短信验证码，手机号为{}，内容是{}",userDTO.getPhone(),code);

        redisRepository.set("valid-code-" + userDTO.getPhone(), code);

        redisRepository.expire("valid-code-" + userDTO.getPhone(),120);

        //LuosimaoDuanxinUtil.SendCode(userDTO.getPhone(),code);
        //LuosimaoDuanxinUtil.sendSpeak(userDTO.getPhone(),code);

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> checkValidCode(String phone ,String validcode) {

        String redisValidCode = redisRepository.get("valid-code-" + phone);
        if(!validcode.equals(redisValidCode)){
            return this.setResultError("验证码输入错误");
        }

        return this.setResultSuccess();
    }
}
