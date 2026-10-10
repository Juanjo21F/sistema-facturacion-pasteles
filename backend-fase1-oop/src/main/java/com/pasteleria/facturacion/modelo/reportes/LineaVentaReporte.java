package com.pasteleria.facturacion.modelo.reportes;

import com.pasteleria.facturacion.modelo.enumeraciones.EstadoFactura;

import java.math.BigDecimal;

/** Una línea vendida tal como la entrega la consulta del reporte (antes de agrupar). */
public record LineaVentaReporte(Long productoId, String codigoProducto, String nombreProducto,
                                int cantidad, BigDecimal subtotal, EstadoFactura estadoFactura) {
}
