package com.baidu.shop.business.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.PayService;
import com.baidu.shop.config.AlipayConfig;
import com.baidu.shop.dto.OrderInfo;
import com.baidu.shop.dto.PayInfoDTO;
import com.baidu.shop.feign.OrderFeign;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName PayServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/22
 * @Version V1.0
 **/
@Controller
public class PayServiceImpl extends BaseApiService implements PayService {

    @Resource
    private OrderFeign orderFeign;

    @Override
    public void requestPay(PayInfoDTO payInfoDTO, HttpServletResponse response) {

        try {
            //获得初始化的AlipayClient
            AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

            //设置请求参数
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setReturnUrl(AlipayConfig.return_url);
            alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

            Result<OrderInfo> orderInfoResult = orderFeign.getOrderInfoByOrderId(payInfoDTO.getOrderId());

            if (orderInfoResult.getCode() == 200){
                OrderInfo orderInfo = orderInfoResult.getData();
                List<String> collect = orderInfo.getOrderDetailList().stream().map(orderDetailEntity -> orderDetailEntity.getTitle()).collect(Collectors.toList());

                String titleStr = String.join(",", collect);
                titleStr = titleStr.length() > 10 ? titleStr.substring(0,10) : titleStr;
                //商户订单号，商户网站订单系统中唯一订单号，必填
                String out_trade_no = payInfoDTO.getOrderId() + "";
                //付款金额，必填
                String total_amount = Double.valueOf(orderInfo.getActualPay()) / 100 + "";
                //订单名称，必填
                String subject = titleStr;
                //商品描述，可空
                String body = "";

                alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                        + "\"total_amount\":\""+ total_amount +"\","
                        + "\"subject\":\""+ subject +"\","
                        + "\"body\":\""+ body +"\","
                        + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

                //请求
                String result = alipayClient.pageExecute(alipayRequest).getBody();

                response.setContentType("text/html; charset=utf-8");

                //输出
                response.getWriter().println(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void notify(HttpServletRequest httpServletRequest) {

        try {
            //获取支付宝POST过来反馈信息
            Map<String,String> params = new HashMap<String,String>();
            Map<String,String[]> requestParams = httpServletRequest.getParameterMap();
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                params.put(name, valueStr);
            }

            boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名

            if(signVerified) {//验证成功
                //商户订单号
                String out_trade_no = new String(httpServletRequest.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

                //支付宝交易号
                String trade_no = new String(httpServletRequest.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

                //交易状态
                String trade_status = new String(httpServletRequest.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");

                if(trade_status.equals("TRADE_FINISHED")){

                }else if (trade_status.equals("TRADE_SUCCESS")){

                }

                //out.println("success");

                //处理业务逻辑

            }else {//验证失败
                //out.println("fail");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void returnHTML(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            httpServletResponse.setContentType("text/html; charset=utf-8");
            //获取支付宝GET过来反馈信息
            Map<String,String> params = new HashMap<String,String>();
            Map<String,String[]> requestParams = httpServletRequest.getParameterMap();
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                params.put(name, valueStr);
            }

            boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名

            //——请在这里编写您的程序（以下代码仅作参考）——
            if(signVerified) {
                //商户订单号
                String out_trade_no = new String(httpServletRequest.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

                //支付宝交易号
                String trade_no = new String(httpServletRequest.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

                //付款金额
                String total_amount = new String(httpServletRequest.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

                httpServletResponse.sendRedirect("http://www.mrshop.com/success.html?orderId=" + out_trade_no + "&totalPay=" + total_amount);
            }else {
                //out.println("验签失败");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
