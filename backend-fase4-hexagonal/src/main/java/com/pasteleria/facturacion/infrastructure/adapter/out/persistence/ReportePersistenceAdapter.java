package com.pasteleria.facturacion.infrastructure.adapter.out.persistence;

import com.pasteleria.facturacion.application.port.out.ReporteRepositoryPort;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository.DetalleFacturaRepository;
import com.pasteleria.facturacion.domain.model.LineaVentaReporte;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReportePersistenceAdapter implements ReporteRepositoryPort {

    private final DetalleFacturaRepository detalles;

    public ReportePersistenceAdapter(DetalleFacturaRepository detalles) {
        this.detalles = detalles;
    }

    @Override
    public List<LineaVentaReporte> buscarLineasVendidas(LocalDateTime desde, LocalDateTime hasta) {
        return detalles.findFilasEntre(desde, hasta).stream()
                .map(f -> new LineaVentaReporte(f.productoId(), f.codigoUnico(), f.nombre(),
                        f.cantidad(), f.subtotal(), f.estadoFactura()))
                .toList();
    }
}
