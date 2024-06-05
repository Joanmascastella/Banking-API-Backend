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

    @Value("Bearer: eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI1IiwiYXV0aCI6IlJPTEVfRU1QTE9ZRUUiLCJhcHByb3ZlZCI6InRydWUiLCJpYXQiOjE3MTc1OTk1NTksImV4cCI6MTcxNzYwMzE1OX0.v9no_ZIouMiznyYtYO0F3TGSGQTQ-Z7hvaUmySpfK3GNbyasJ3nk_uhtym4JHw7EbeD_xoGiKRSN6bKkWbDpJ0Cy-2vuiL66nXlGFk1EIwwWaOFcSDZR1HKQckzgnH_bCxaWLWa8dJSyljPKeb5gf_YhIQobWMlNk74cOxstD48szlb9P9Lg7s02fYHEvFc-uRux175ezSio7FppCt_R-_v19k94RYr3cc12kfoSGlXFX9r3a9p7f4CeC_f2mg7LfkTByN_hrI2O2d_4Q7kzsuh_NchaK_wVP3fWvc4aZdPsoYRRY481hVsjHw45Dlupbvzntr_epS7Ug3flZKuJSA")
    private String token;
}
