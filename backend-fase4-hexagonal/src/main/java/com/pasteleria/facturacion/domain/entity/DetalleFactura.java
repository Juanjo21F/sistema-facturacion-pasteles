package com.pasteleria.facturacion.domain.entity;

import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.valueobject.LineaVenta;

import java.math.BigDecimal;
import java.util.Objects;

public class DetalleFactura {

    private final Long idDetalle;
    private final Producto producto;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public DetalleFactura(Long idDetalle, Producto producto, int cantidad, BigDecimal precioUnitario) {
        Validaciones.cantidadPositiva(cantidad);
        Validaciones.mayorQueCero(precioUnitario, "precioUnitario");
        if (producto == null) {
            throw new ReglaDeNegocioException("El detalle debe tener un producto");
        }
        this.idDetalle = idDetalle;
        this.producto = Objects.requireNonNull(producto);
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad)); // cantidad × precioUnitario
    }

    /** El precio unitario se toma del producto en el momento de la venta. */
    public static DetalleFactura desde(LineaVenta linea) {
        return new DetalleFactura(null, linea.producto(), linea.cantidad(), linea.producto().getPrecio());
    }

    public Long getIdDetalle() { return idDetalle; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
