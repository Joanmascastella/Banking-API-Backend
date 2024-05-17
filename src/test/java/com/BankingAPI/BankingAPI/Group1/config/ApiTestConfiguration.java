package com.BankingAPI.BankingAPI.Group1.config;

import com.BankingAPI.BankingAPI.Group1.util.JwtTokenProvider;
import org.springframework.boot.test.mock.mockito.MockBean;

@org.springframework.boot.test.context.TestConfiguration
public class ApiTestConfiguration {

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
}
