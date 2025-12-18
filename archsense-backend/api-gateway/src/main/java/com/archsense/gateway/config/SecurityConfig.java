//package com.archsense.gateway.config;
//
//import com.archsense.gateway.filter.JwtAuthenticationFilter;
//import com.archsense.gateway.filter.ProxyFilter;
//import com.archsense.gateway.filter.RateLimitFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter(JwtAuthenticationFilter filter) {
//        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(filter);
//        registration.addUrlPatterns("/api/*");
//        registration.setOrder(1);
//        return registration;
//    }
//
//    @Bean
//    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(RateLimitFilter filter) {
//        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(filter);
//        registration.addUrlPatterns("/api/*");
//        registration.setOrder(2);
//        return registration;
//    }
//
//    @Bean
//    public FilterRegistrationBean<ProxyFilter> proxyFilter(ProxyFilter filter) {
//        FilterRegistrationBean<ProxyFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(filter);
//        registration.addUrlPatterns("/api/*");
//        registration.setOrder(3);
//        return registration;
//    }
//}