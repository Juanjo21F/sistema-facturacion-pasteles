package com.pasteleria.facturacion.infrastructure.configuration;

import com.pasteleria.facturacion.domain.service.FacturacionDomainService;
import com.pasteleria.facturacion.domain.service.InventarioDomainService;
import com.pasteleria.facturacion.domain.service.ReporteVentasDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Registra los servicios de dominio (que no usan anotaciones de Spring) como beans. */
@Configuration
public class DomainConfiguration {

    @Bean
    public InventarioDomainService inventarioDomainService() {
        return new InventarioDomainService();
    }

    @Bean
    public FacturacionDomainService facturacionDomainService(InventarioDomainService inventario) {
        return new FacturacionDomainService(inventario);
    }

    @Bean
    public ReporteVentasDomainService reporteVentasDomainService() {
        return new ReporteVentasDomainService();
    }

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
