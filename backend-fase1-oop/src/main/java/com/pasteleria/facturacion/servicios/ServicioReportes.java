package com.pasteleria.facturacion.servicios;

import com.pasteleria.facturacion.modelo.reportes.LineaVentaReporte;
import com.pasteleria.facturacion.modelo.reportes.PeriodoMensual;
import com.pasteleria.facturacion.modelo.reportes.ReporteProductoVendido;
import com.pasteleria.facturacion.modelo.reportes.ReporteProductosMasVendidos;
import com.pasteleria.facturacion.repositorios.DetalleFacturaRepositorio;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicioReportes {

    private final DetalleFacturaRepositorio detalles;

    public ServicioReportes(DetalleFacturaRepositorio detalles) {
        this.detalles = detalles;
    }

    public List<ReporteProductoVendido> ventasDelMes(int mes, int anio) {
        PeriodoMensual periodo = new PeriodoMensual(mes, anio);
        List<LineaVentaReporte> lineas = detalles.findFilasEntre(periodo.desde(), periodo.hasta()).stream()
                .map(f -> new LineaVentaReporte(f.productoId(), f.codigoUnico(), f.nombre(),
                        f.cantidad(), f.subtotal(), f.estadoFactura()))
                .toList();
        return new ReporteProductosMasVendidos().generar(lineas);
    }
}
