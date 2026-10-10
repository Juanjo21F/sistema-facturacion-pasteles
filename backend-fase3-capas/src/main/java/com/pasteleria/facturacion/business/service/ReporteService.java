package com.pasteleria.facturacion.business.service;

import com.pasteleria.facturacion.business.dto.LineaVentaReporte;
import com.pasteleria.facturacion.business.dto.PeriodoMensual;
import com.pasteleria.facturacion.business.dto.ReporteMensualResponse;
import com.pasteleria.facturacion.business.dto.ReporteProductoVendido;
import com.pasteleria.facturacion.business.mapper.EntityMapper;
import com.pasteleria.facturacion.dataaccess.entity.EstadoFactura;
import com.pasteleria.facturacion.dataaccess.repository.DetalleFacturaRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capa de negocio: reporte mensual. Excluye ANULADAS, agrupa por producto y ordena por cantidad descendente. */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final DetalleFacturaRepository detalles;

    public ReporteService(DetalleFacturaRepository detalles) {
        this.detalles = detalles;
    }

    public ReporteMensualResponse ventasDelMes(int mes, int anio) {
        PeriodoMensual periodo = new PeriodoMensual(mes, anio);
        List<LineaVentaReporte> lineas = detalles.findFilasEntre(periodo.desde(), periodo.hasta()).stream()
                .map(f -> new LineaVentaReporte(f.productoId(), f.codigoUnico(), f.nombre(),
                        f.cantidad(), f.subtotal(), f.estadoFactura()))
                .toList();
        return EntityMapper.aRespuesta(mes, anio, consolidar(lineas));
    }

    /** Regla pura (sin base de datos): fácil de probar. */
    public List<ReporteProductoVendido> consolidar(List<LineaVentaReporte> lineas) {
        Map<Long, List<LineaVentaReporte>> porProducto = lineas.stream()
                .filter(l -> l.estadoFactura() != EstadoFactura.ANULADA)
                .collect(Collectors.groupingBy(LineaVentaReporte::productoId));
        return porProducto.values().stream()
                .map(this::agrupar)
                .sorted(Comparator.comparingLong(ReporteProductoVendido::cantidadVendida).reversed()
                        .thenComparing(ReporteProductoVendido::nombreProducto))
                .toList();
    }

    private ReporteProductoVendido agrupar(List<LineaVentaReporte> lineasDelProducto) {
        LineaVentaReporte primera = lineasDelProducto.get(0);
        long cantidad = lineasDelProducto.stream().mapToLong(LineaVentaReporte::cantidad).sum();
        BigDecimal ingresos = lineasDelProducto.stream()
                .map(LineaVentaReporte::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReporteProductoVendido(primera.productoId(), primera.codigoProducto(),
                primera.nombreProducto(), cantidad, ingresos);
    }
}
