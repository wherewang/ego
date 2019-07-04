package com.ego.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author yaorange
 * @date 2018/10/5
 */
@Configuration
public class PayConfiguration {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }

    @Bean
    @ConfigurationProperties(prefix = "ego.pay")
    public PayConfig payConfig() {
        return new PayConfig();
    }
}
