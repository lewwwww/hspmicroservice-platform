package com.hspliving.service;

import com.alibaba.alicloud.context.oss.OssContextAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @version 1.0
 */

@EnableDiscoveryClient
@SpringBootApplication
public class HsplivingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HsplivingServiceApplication.class, args);
    }
}
