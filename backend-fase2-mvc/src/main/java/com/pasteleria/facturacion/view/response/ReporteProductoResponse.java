package com.pasteleria.facturacion.view.response;

import java.math.BigDecimal;

public record ReporteProductoResponse(Long idProducto, String codigoUnico, String producto,
                                      long cantidadVendida, BigDecimal totalIngresos) {
}
