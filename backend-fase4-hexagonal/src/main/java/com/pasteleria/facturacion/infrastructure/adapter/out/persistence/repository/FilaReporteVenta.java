package com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository;

import com.pasteleria.facturacion.domain.model.EstadoFactura;

import java.math.BigDecimal;

/** Proyección de una línea vendida (resultado de la consulta del reporte). */
public record FilaReporteVenta(Long productoId, String codigoUnico, String nombre, Integer cantidad,
                               BigDecimal subtotal, EstadoFactura estadoFactura) {
}
