package com.pasteleria.facturacion.domain.service;

import com.pasteleria.facturacion.domain.valueobject.EstadoFactura;
import com.pasteleria.facturacion.domain.valueobject.LineaVentaReporte;
import com.pasteleria.facturacion.domain.valueobject.ReporteProductoVendido;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Reglas del reporte: excluir ANULADAS, agrupar por producto y ordenar por cantidad descendente. */
public class ReporteVentasDomainService {

    public List<ReporteProductoVendido> generar(List<LineaVentaReporte> lineas) {
        Map<Long, List<LineaVentaReporte>> porProducto = lineas.stream()
                .filter(l -> l.estadoFactura() != EstadoFactura.ANULADA)
                .collect(Collectors.groupingBy(LineaVentaReporte::productoId));

        return porProducto.values().stream()
                .map(this::consolidar)
                .sorted(Comparator.comparingLong(ReporteProductoVendido::cantidadVendida).reversed()
                        .thenComparing(ReporteProductoVendido::nombreProducto))
                .toList();
    }

    private ReporteProductoVendido consolidar(List<LineaVentaReporte> lineasDelProducto) {
        LineaVentaReporte primera = lineasDelProducto.get(0);
        long cantidad = lineasDelProducto.stream().mapToLong(LineaVentaReporte::cantidad).sum();
        BigDecimal ingresos = lineasDelProducto.stream()
                .map(LineaVentaReporte::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReporteProductoVendido(primera.productoId(), primera.codigoProducto(),
                primera.nombreProducto(), cantidad, ingresos);
    }
}
