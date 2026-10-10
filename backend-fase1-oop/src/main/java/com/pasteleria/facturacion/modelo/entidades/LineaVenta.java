package com.pasteleria.facturacion.modelo.entidades;

/** Producto (ya cargado) y cantidad que se quiere vender. */
public record LineaVenta(Producto producto, int cantidad) {
}
