package com.pasteleria.facturacion.domain.model;

import java.math.BigDecimal;

public record ReporteProductoVendido(Long productoId, String codigoProducto, String nombreProducto,
                                     long cantidadVendida, BigDecimal totalIngresos) {
}
