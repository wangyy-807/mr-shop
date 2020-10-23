package com.baidu.shop.config;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @ClassName AlipayConfig
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/22
 * @Version V1.0
 **/
public class AlipayConfig {

    //↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016102600766639";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7gi3NWI4CWr1Mm3sRmebqgiGurjV1NBVKmqVDbE1CIUgadOilaHdjW5GvBPC48Kpq2gxIRiCsrC/WdHPg88lAJseutKrfOrdVraxWzGGTT11T0X6A9rnpvRF+c8Oz5HpIB+6vPpldbabVgSI2/XoRe4n3wpiFWVnI9o9lmQm3LfPQEofE/OhKSw6JAdupoGjGjRrxcigGTm61EQx3tMzfL8C0Pl3lcOjhtYfOOBQVkNUfGAvaFEZ5xzfkEK66BbyOfdLGm8csj4WV3KpvqgW6C/sD+G6G1JMyvXNkytzIUYaS+LYVJ3l2TggY5C3Gq7CEfNCa44fUt95MHd8RPPa5AgMBAAECggEBAJhNdMaGV1KyQXAoGh6B035HlPQ9rE42r9SZKANLxLUiM17J9u3jnJWnmv1+FckiMkn4MmHCFGgss+6NSFI/sgfZd/f/54rKNn/xQTp/aAIvRK8ASrCVtwyT9dGwVhWYT3qSdt0BC7FHj2t8KD5iRPROVBJajWhC2xrQqkjrtWiDFrXwqkCaOMTcN3wk92n0xXU+E7dBunUCNX0fu2/jZCZheIWUC51hC7iziLisF8vMK7XCpKoXgp7ZOrvp0NrPbYsyHwJqSjfcdsfx7hl4326UXzN25MlVvwyy7mV+cqfiNbHS19CVfyFRNf/23qLzu8mIADFMACOrAs+VRnX4wMECgYEA39t9sVfgYf82iJ6ooFrNHAFfqF4w3iPfbSWZiYGjuhB5gLtqWYhXwvRXpCVDS5omiZ0cRhWJrmjpmVV77Ucfk6KeYBbJUJTPHys4g5scI0zxhhytKgQSqWCU+IGLasjiyb1+0Z9rX62n0eHAlzmP97jMEZ5VoIpGkgW0h5bJZE8CgYEA1m6VJPtwvFBdkAg4gy5LpYSaSIl06Ujdc0pEYMyFeUC4GhSfTZwOaKttbPNDV8t/Q+F7TJfaokTmutkybxgZ54r/yxN/6opUrpImUMm/jIS1IRJHEqYGbp5Jb8RUpKzE2iGI7XI1Ste6pO9wseWVw7w0vSwMZWg+e3gUcOS3yncCgYAq1VjLokDw/2Bz68fgRv+QKEMheERpfPu3asZiJUgqxhQ5qaLD1u7UAuTyJ58kzC4rjsBmOKDLmo94I38++6jzGt6rDDEhFhg7oo2BwSA1MPfV7Y7Ex1q6xhw7IK6IlD+w0m19tr9Y42WFJ2kuhnkhsV560I0Z1tuB/U151ERcyQKBgFvqMswTa5OM704Z9/9jDNH6MRf+s7p7nVEZ+I11nZEICOJFDLP7vMFpGhKlg6xSck7qjCucu/7qgex9xXyRXClAD71Asogam4no47ulfxIAoJN4fjakLAWmm9vo0E92LztBPKxrVCSsrDie2jOnMbchKYodI4MkWAQcyMZLg+ypAoGARcGQJzj1Vywdmpk625v7YX3P/EH+FclCYZCsVfiZ1jo2OowDrqG3s9P3psQnQvpgvv98ti024Cqs50Qaz4y10JCynQqITDPZilkZaiJR1nRkSOcmlOE0iGpx2OkXHIYx1bnB0DsjHJnz2qcy/vldl+gQXNfRAjkNo1RgBL9lQX0=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlEJyP2wi5OTHp6oxmAMN9OhBvIUWY3Vh+SAwfeVAnkyWHIRkUMnE78rPSncHC0fOVJ7sXU206jT6K4fCjuhseRvT1k4LVygZKxa0XaXKvwak7ctlg/IoVfEKHqcMd7NyUm0QfHYx5YFGiyrhu1q81UmJJcYVYLpCNwGIGiHZ78oW6Ojbf68VbcanP+57joz13QBMeZlyO/pppt9o++wltdP9G/P0E6uLBsqJ7aF5YjdiL4pZDBw1q5ivLzLc+WNGPtVKEnceV6TONblEDQdomhrCaBXI2YS/IDOrgwkZHG8uOulSrAPblzHKNUI9vGT85orP1tpz327J2pEqLKdWiQIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://localhost:8900/pay/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://localhost:8900/pay/return";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "C:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
