package com.baidu.shop.business;

import com.baidu.shop.dto.PayInfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName PayService
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/22
 * @Version V1.0
 **/
@Api(tags = "支付接口")
public interface PayService {

    @ApiOperation(value = "请求支付")
    @GetMapping(value = "pay/requestPay")//请求支付
    void requestPay(PayInfoDTO payInfoDTO, HttpServletResponse response);

    @ApiOperation(value = "通知接口,这个可能暂时测试不了")
    @GetMapping(value = "pay/notify")//通知接口,这个可能暂时测试不了
    void notify(HttpServletRequest httpServletRequest);

    @ApiOperation(value = "跳转成功页面接口")
    @GetMapping(value = "pay/return")//跳转成功页面接口
    void returnHTML(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse);

}
