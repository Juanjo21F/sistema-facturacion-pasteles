package com.pasteleria.facturacion.model.service;

import com.pasteleria.facturacion.model.report.LineaVentaReporte;
import com.pasteleria.facturacion.model.report.PeriodoMensual;
import com.pasteleria.facturacion.model.report.ReporteProductoVendido;
import com.pasteleria.facturacion.model.report.ReporteVentas;
import com.pasteleria.facturacion.model.repository.DetalleFacturaRepository;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Modelo: reporte mensual de productos más vendidos (una sola consulta, sin N+1). */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final DetalleFacturaRepository detalles;

    public ReporteService(DetalleFacturaRepository detalles) {
        this.detalles = detalles;
    }

    public List<ReporteProductoVendido> ventasDelMes(int mes, int anio) {
        PeriodoMensual periodo = new PeriodoMensual(mes, anio);
        List<LineaVentaReporte> lineas = detalles.findFilasEntre(periodo.desde(), periodo.hasta()).stream()
                .map(f -> new LineaVentaReporte(f.productoId(), f.codigoUnico(), f.nombre(),
                        f.cantidad(), f.subtotal(), f.estadoFactura()))
                .toList();
        return ReporteVentas.generar(lineas);
    }
}
