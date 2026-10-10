package com.pasteleria.facturacion.model.report;

import java.math.BigDecimal;

public record ReporteProductoVendido(Long productoId, String codigoProducto, String nombreProducto,
                                     long cantidadVendida, BigDecimal totalIngresos) {
}
