package com.ego.upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EgoUploadService {
    public static void main(String[] args) {
        SpringApplication.run(EgoUploadService.class, args);
    }
}