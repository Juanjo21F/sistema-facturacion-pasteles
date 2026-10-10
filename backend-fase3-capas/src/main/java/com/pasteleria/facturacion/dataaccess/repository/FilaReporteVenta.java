package com.pasteleria.facturacion.dataaccess.repository;

import com.pasteleria.facturacion.dataaccess.entity.EstadoFactura;

import java.math.BigDecimal;

/** Proyección de una línea vendida (resultado de la consulta del reporte). */
public record FilaReporteVenta(Long productoId, String codigoUnico, String nombre, Integer cantidad,
                               BigDecimal subtotal, EstadoFactura estadoFactura) {
}
