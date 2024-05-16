package com.BankingAPI.BankingAPI.Group1.config.testConfigurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class TestConfig {

    @Value("${test.base.url}")
    private String baseUrl;

    @Value("${test.username}")
    private String username;

    @Value("${test.password}")
    private String password;
}
