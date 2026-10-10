package com.pasteleria.facturacion.presentation.dto;

import java.math.BigDecimal;

public record ReporteProductoResponse(Long idProducto, String codigoUnico, String producto,
                                      long cantidadVendida, BigDecimal totalIngresos) {
}
