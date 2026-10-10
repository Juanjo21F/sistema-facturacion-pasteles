package com.pasteleria.facturacion.business.dto;

import java.math.BigDecimal;

public record ReporteProductoResponse(Long idProducto, String codigoUnico, String producto,
                                      long cantidadVendida, BigDecimal totalIngresos) {
}
