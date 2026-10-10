package com.pasteleria.facturacion.domain.valueobject;

import com.pasteleria.facturacion.domain.entity.Producto;

/** Producto (ya cargado) y cantidad que se quiere vender. */
public record LineaVenta(Producto producto, int cantidad) {
}
