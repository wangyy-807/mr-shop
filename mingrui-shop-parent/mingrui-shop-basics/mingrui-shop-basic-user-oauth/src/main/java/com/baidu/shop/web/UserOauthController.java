package com.baidu.shop.web;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.UserOauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.StringUtil;
import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName UserOauthController
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/15
 * @Version V1.0
 **/
@RestController
@RequestMapping(value = "oauth")
public class UserOauthController extends BaseApiService {

    @Resource
    private UserOauthService userOauthService;

    @Resource
    private JwtConfig jwtConfig;

    @PostMapping(value = "login")
    public Result<JsonObject> login(@RequestBody UserEntity userEntity, HttpServletRequest request, HttpServletResponse response){

        String token = userOauthService.login(userEntity,jwtConfig);

        if (StringUtil.isEmpty(token)){
            return this.setResultError(5002,"用户名或密码错误");
        }

        CookieUtils.setCookie(request,response,jwtConfig.getCookieName(),token,jwtConfig.getCookieMaxAge(),true);

        return this.setResultSuccess();

    }

    @GetMapping(value = "verify")
    public Result<UserInfo> verifyUser(@CookieValue(value = "MRSHOP_TOKEN") String token
            , HttpServletRequest request , HttpServletResponse response){

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            String newToken = JwtUtils.generateToken(userInfo,jwtConfig.getPrivateKey(),jwtConfig.getExpire());

            CookieUtils.setCookie(request,response,jwtConfig.getCookieName(),newToken,jwtConfig.getCookieMaxAge(),true);

            return this.setResultSuccess(userInfo);
        } catch (Exception e) {//如果有异常 说明token有问题
            //e.printStackTrace();
            //应该新建http状态为用户验证失败,状态码为403
            return this.setResultError(403,"");
        }
    }
}
