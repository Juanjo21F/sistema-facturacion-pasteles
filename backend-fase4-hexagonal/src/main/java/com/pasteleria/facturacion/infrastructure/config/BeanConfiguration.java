package com.pasteleria.facturacion.infrastructure.config;

import com.pasteleria.facturacion.application.port.in.AutenticacionUseCase;
import com.pasteleria.facturacion.application.port.in.ConsultarReporteUseCase;
import com.pasteleria.facturacion.application.port.in.FacturacionUseCase;
import com.pasteleria.facturacion.application.port.in.GestionarClientesUseCase;
import com.pasteleria.facturacion.application.port.in.GestionarProductosUseCase;
import com.pasteleria.facturacion.application.port.out.AuditoriaPort;
import com.pasteleria.facturacion.application.port.out.AutenticacionPort;
import com.pasteleria.facturacion.application.port.out.CategoriaRepositoryPort;
import com.pasteleria.facturacion.application.port.out.ClienteRepositoryPort;
import com.pasteleria.facturacion.application.port.out.FacturaRepositoryPort;
import com.pasteleria.facturacion.application.port.out.ProductoRepositoryPort;
import com.pasteleria.facturacion.application.port.out.ReporteRepositoryPort;
import com.pasteleria.facturacion.application.service.AutenticacionService;
import com.pasteleria.facturacion.application.service.ClienteService;
import com.pasteleria.facturacion.application.service.FacturacionService;
import com.pasteleria.facturacion.application.service.ProductoService;
import com.pasteleria.facturacion.application.service.ReporteService;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Compone el hexágono: crea los servicios de aplicación (que no conocen Spring) y los conecta con los
 * adaptadores de salida a través de los puertos.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public GestionarProductosUseCase gestionarProductos(ProductoRepositoryPort productos,
                                                        CategoriaRepositoryPort categorias, AuditoriaPort auditoria) {
        return new ProductoService(productos, categorias, auditoria);
    }

    @Bean
    public GestionarClientesUseCase gestionarClientes(ClienteRepositoryPort clientes, AuditoriaPort auditoria) {
        return new ClienteService(clientes, auditoria);
    }

    @Bean
    public FacturacionUseCase facturacion(ClienteRepositoryPort clientes, ProductoRepositoryPort productos,
                                          FacturaRepositoryPort facturas, AuditoriaPort auditoria, Clock reloj) {
        return new FacturacionService(clientes, productos, facturas, auditoria, reloj);
    }

    @Bean
    public ConsultarReporteUseCase reportes(ReporteRepositoryPort reportes) {
        return new ReporteService(reportes);
    }

    @Bean
    public AutenticacionUseCase autenticacion(AutenticacionPort authcore) {
        return new AutenticacionService(authcore);
    }
}
