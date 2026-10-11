package com.pasteleria.facturacion.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record ReporteProductoResponse(Long idProducto, String codigoUnico, String producto,
                                      long cantidadVendida, BigDecimal totalIngresos) {
}
