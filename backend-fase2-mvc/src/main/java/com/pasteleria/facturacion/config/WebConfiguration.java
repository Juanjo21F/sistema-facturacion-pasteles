package com.pasteleria.facturacion.config;

import com.pasteleria.facturacion.controller.security.AutorizacionInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final AutorizacionInterceptor autorizacion;

    public WebConfiguration(AutorizacionInterceptor autorizacion) {
        this.autorizacion = autorizacion;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autorizacion).addPathPatterns("/api/**");
    }
}
