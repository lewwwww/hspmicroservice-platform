package com.hspliving.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @version 1.0
 */
@EnableDiscoveryClient
@SpringBootApplication
public class HsplivingGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(HsplivingGatewayApplication.class, args);
    }
}
