package com.ego.seckill;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author: yaorange
 * @Time: 2019-03-30 10:57
 * @Feature:
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("com.ego.seckill.mapping")
public class EgoSeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(EgoSeckillApplication.class,args);
    }
}
