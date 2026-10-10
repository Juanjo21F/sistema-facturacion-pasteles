package com.pasteleria.facturacion.modelo.entidades;

import com.pasteleria.facturacion.modelo.base.EntidadBase;
import com.pasteleria.facturacion.modelo.base.Validaciones;

import jakarta.persistence.AttributeOverride;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_factura")
@AttributeOverride(name = "id", column = @Column(name = "id_detalle"))
public class DetalleFactura extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_factura", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    protected DetalleFactura() {
    }

    /** El precio unitario se toma del producto en el momento de la venta; subtotal = cantidad × precio. */
    DetalleFactura(LineaVenta linea) {
        Validaciones.cantidadPositiva(linea.cantidad());
        this.producto = linea.producto();
        this.cantidad = linea.cantidad();
        this.precioUnitario = linea.producto().getPrecio();
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    void asignarFactura(Factura factura) {
        this.factura = factura;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
