package com.pasteleria.facturacion.model.report;

import com.pasteleria.facturacion.model.enums.EstadoFactura;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Modelo: reglas del reporte. Excluye ANULADAS, agrupa por producto y ordena por cantidad descendente. */
public final class ReporteVentas {

    private ReporteVentas() {
    }

    public static List<ReporteProductoVendido> generar(List<LineaVentaReporte> lineas) {
        Map<Long, List<LineaVentaReporte>> porProducto = lineas.stream()
                .filter(l -> l.estadoFactura() != EstadoFactura.ANULADA)
                .collect(Collectors.groupingBy(LineaVentaReporte::productoId));
        return porProducto.values().stream()
                .map(ReporteVentas::consolidar)
                .sorted(Comparator.comparingLong(ReporteProductoVendido::cantidadVendida).reversed()
                        .thenComparing(ReporteProductoVendido::nombreProducto))
                .toList();
    }

    private static ReporteProductoVendido consolidar(List<LineaVentaReporte> lineasDelProducto) {
        LineaVentaReporte primera = lineasDelProducto.get(0);
        long cantidad = lineasDelProducto.stream().mapToLong(LineaVentaReporte::cantidad).sum();
        BigDecimal ingresos = lineasDelProducto.stream()
                .map(LineaVentaReporte::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReporteProductoVendido(primera.productoId(), primera.codigoProducto(),
                primera.nombreProducto(), cantidad, ingresos);
    }
}
