package com.pasteleria.facturacion.domain.model;

import java.util.List;

public record ReporteMensual(int mes, int anio, List<ReporteProductoVendido> productos) {

    /** Regla pura: excluye ANULADAS, agrupa por producto y ordena por cantidad descendente. */
    public static ReporteMensual consolidar(int mes, int anio, List<LineaVentaReporte> lineas) {
        var porProducto = lineas.stream()
                .filter(l -> l.estadoFactura() != EstadoFactura.ANULADA)
                .collect(java.util.stream.Collectors.groupingBy(LineaVentaReporte::productoId));
        List<ReporteProductoVendido> filas = porProducto.values().stream()
                .map(ReporteMensual::agrupar)
                .sorted(java.util.Comparator.comparingLong(ReporteProductoVendido::cantidadVendida).reversed()
                        .thenComparing(ReporteProductoVendido::nombreProducto))
                .toList();
        return new ReporteMensual(mes, anio, filas);
    }

    private static ReporteProductoVendido agrupar(List<LineaVentaReporte> lineas) {
        LineaVentaReporte primera = lineas.get(0);
        long cantidad = lineas.stream().mapToLong(LineaVentaReporte::cantidad).sum();
        java.math.BigDecimal ingresos = lineas.stream().map(LineaVentaReporte::subtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return new ReporteProductoVendido(primera.productoId(), primera.codigoProducto(),
                primera.nombreProducto(), cantidad, ingresos);
    }
}
