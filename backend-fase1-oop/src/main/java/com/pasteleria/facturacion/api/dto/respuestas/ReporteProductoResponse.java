package com.pasteleria.facturacion.api.dto.respuestas;

import java.math.BigDecimal;

public record ReporteProductoResponse(Long idProducto, String codigoUnico, String producto,
                                      long cantidadVendida, BigDecimal totalIngresos) {
}
