package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @ClassName RunUserServerApplication
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/13
 * @Version V1.0
 **/
@SpringBootApplication
@EnableFeignClients
@MapperScan(value = "com.baidu.shop.mapper")
public class RunUserServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunUserServerApplication.class,args);
    }
}
