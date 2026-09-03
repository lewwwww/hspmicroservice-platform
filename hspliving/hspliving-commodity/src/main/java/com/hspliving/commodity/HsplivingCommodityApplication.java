package com.hspliving.commodity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @version 1.0
 */
//@MapperScan指定扫描的dao包, 如果我们在Dao指定的有 @Mapper , 也可以不写
//@MapperScan("com.hspliving.commodity.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class HsplivingCommodityApplication {
    public static void main(String[] args) {
        SpringApplication.run(HsplivingCommodityApplication.class, args);
    }
}
