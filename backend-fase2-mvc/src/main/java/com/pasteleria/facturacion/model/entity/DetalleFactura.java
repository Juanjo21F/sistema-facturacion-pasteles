package com.pasteleria.facturacion.model.entity;

import com.pasteleria.facturacion.exception.ReglaDeNegocioException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Modelo: línea de una factura. El subtotal siempre es cantidad × precioUnitario. */
@Entity
@Table(name = "detalles_factura")
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

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

    public DetalleFactura(Producto producto, int cantidad, BigDecimal precioUnitario) {
        Validaciones.cantidadPositiva(cantidad);
        Validaciones.mayorQueCero(precioUnitario, "precioUnitario");
        if (producto == null) {
            throw new ReglaDeNegocioException("El detalle debe tener un producto");
        }
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    /** El precio unitario se toma del producto en el momento de la venta. */
    static DetalleFactura desde(LineaVenta linea) {
        return new DetalleFactura(linea.producto(), linea.cantidad(), linea.producto().getPrecio());
    }

    void asignarFactura(Factura factura) {
        this.factura = factura;
    }

    public Long getIdDetalle() { return idDetalle; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
