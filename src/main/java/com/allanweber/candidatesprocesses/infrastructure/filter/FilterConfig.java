package com.allanweber.candidatesprocesses.infrastructure.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<HealthFilter> registrySwaggerFilter(){
        FilterRegistrationBean<HealthFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        HealthFilter swaggerFilter = new HealthFilter();
        filterRegistrationBean.setFilter(swaggerFilter);
        return filterRegistrationBean;
    }
}
