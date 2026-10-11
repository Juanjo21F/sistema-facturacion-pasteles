package com.pasteleria.facturacion.domain.model;

import java.math.BigDecimal;

/** Una línea vendida tal como la entrega el puerto de reportes (antes de agrupar). */
public record LineaVentaReporte(Long productoId, String codigoProducto, String nombreProducto,
                                int cantidad, BigDecimal subtotal, EstadoFactura estadoFactura) {
}
