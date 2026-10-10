package com.pasteleria.facturacion.infrastructure.persistence.adapter;

import com.pasteleria.facturacion.domain.port.output.ReporteVentasPort;
import com.pasteleria.facturacion.domain.valueobject.LineaVentaReporte;
import com.pasteleria.facturacion.domain.valueobject.PeriodoMensual;
import com.pasteleria.facturacion.infrastructure.persistence.repository.DetalleFacturaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReporteVentasAdapter implements ReporteVentasPort {

    private final DetalleFacturaJpaRepository jpa;

    public ReporteVentasAdapter(DetalleFacturaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<LineaVentaReporte> obtenerLineasDelPeriodo(PeriodoMensual periodo) {
        return jpa.findFilasEntre(periodo.desde(), periodo.hasta()).stream()
                .map(f -> new LineaVentaReporte(f.productoId(), f.codigoUnico(), f.nombre(),
                        f.cantidad(), f.subtotal(), f.estadoFactura()))
                .toList();
    }
}
