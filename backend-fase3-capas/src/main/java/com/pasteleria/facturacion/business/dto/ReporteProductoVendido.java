package com.pasteleria.facturacion.business.dto;

import java.math.BigDecimal;

public record ReporteProductoVendido(Long productoId, String codigoProducto, String nombreProducto,
                                     long cantidadVendida, BigDecimal totalIngresos) {
}
