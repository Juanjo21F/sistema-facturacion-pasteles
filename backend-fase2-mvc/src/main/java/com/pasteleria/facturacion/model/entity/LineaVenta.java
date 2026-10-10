package com.pasteleria.facturacion.model.entity;

/** Producto (ya cargado) y cantidad que se quiere vender. */
public record LineaVenta(Producto producto, int cantidad) {
}
