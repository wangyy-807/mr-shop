package com.baidu.shop.business.impl;

import com.baidu.shop.business.UserOauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserOauthMapper;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.StringUtil;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName UserOauthServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Service
public class UserOauthServiceImpl implements UserOauthService {

    @Resource
    private UserOauthMapper userOauthMapper;

    @Override
    public String login(UserEntity userEntity, JwtConfig jwtConfig) {

        String token = null;

        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",userEntity.getUsername());
        List<UserEntity> userEntities = userOauthMapper.selectByExample(example);
        if (userEntities.size() == 1){
            if (BCryptUtil.checkpw(userEntity.getPassword(),userEntities.get(0).getPassword())){
                try {
                    token = JwtUtils.generateToken(new UserInfo(userEntities.get(0).getId(),userEntities.get(0).getUsername()),jwtConfig.getPrivateKey(),jwtConfig.getExpire());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return token;
    }
}
