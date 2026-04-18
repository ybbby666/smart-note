package com.smartnote.config;

import com.smartnote.intercepter.TokenIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenIntercepter tokenIntercepter;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenIntercepter)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/users/register",
                        "/users/login",
                        "/users/login/email",
                        "/users/verification-code",
                        "/users/password/reset",
                        "/users/token/refresh",
                        "/users/logout",
                        "/images/**"
                );
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/images/");
    }
}
