package com.pasteleria.facturacion.domain.model;

import java.math.BigDecimal;

public record DetalleFactura(Long id, Producto producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {

    /** El precio unitario se congela al momento de la venta; el subtotal lo calcula el dominio. */
    public static DetalleFactura de(Producto producto, int cantidad) {
        return new DetalleFactura(null, producto, cantidad, producto.getPrecio(),
                producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
    }
}
