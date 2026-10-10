package com.pasteleria.facturacion.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionAplicacion {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
