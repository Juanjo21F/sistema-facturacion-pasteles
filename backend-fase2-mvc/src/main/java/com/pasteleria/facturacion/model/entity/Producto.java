package com.pasteleria.facturacion.model.entity;

import com.pasteleria.facturacion.exception.ProductoInactivoException;
import com.pasteleria.facturacion.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.exception.StockInsuficienteException;
import com.pasteleria.facturacion.model.enums.EstadoRegistro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Modelo: producto del inventario. Descuenta/restaura su propio stock. */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "codigo_unico", nullable = false, unique = true, length = 50)
    private String codigoUnico;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoRegistro estado;

    protected Producto() {
    }

    private Producto(String codigo, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        this.estado = EstadoRegistro.ACTIVO;
        aplicarDatos(codigo, nombre, categoria, precio, stock);
    }

    public static Producto crear(String codigo, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        return new Producto(codigo, nombre, categoria, precio, stock);
    }

    public void actualizar(String codigo, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        aplicarDatos(codigo, nombre, categoria, precio, stock);
    }

    public void desactivar() {
        this.estado = EstadoRegistro.INACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoRegistro.ACTIVO;
    }

    public void validarDisponibleParaVenta() {
        if (!estaActivo()) {
            throw new ProductoInactivoException(nombre);
        }
    }

    public boolean tieneStockPara(int cantidad) {
        return cantidad <= stock;
    }

    public void descontarStock(int cantidad) {
        Validaciones.cantidadPositiva(cantidad);
        if (!tieneStockPara(cantidad)) {
            throw new StockInsuficienteException(nombre, cantidad, stock);
        }
        this.stock -= cantidad;
    }

    public void restaurarStock(int cantidad) {
        Validaciones.cantidadPositiva(cantidad);
        this.stock += cantidad;
    }

    private void aplicarDatos(String codigo, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        String cod = Validaciones.textoObligatorio(codigo, "codigoUnico");
        String nom = Validaciones.textoObligatorio(nombre, "nombre");
        if (categoria == null) {
            throw new ReglaDeNegocioException("La categoría es obligatoria");
        }
        Validaciones.mayorQueCero(precio, "precio");
        if (stock < 0) {
            throw new ReglaDeNegocioException("El stock no puede ser negativo");
        }
        this.codigoUnico = cod;
        this.nombre = nom;
        this.categoria = categoria;
        this.precio = precio.setScale(2, RoundingMode.HALF_UP);
        this.stock = stock;
    }

    public Long getIdProducto() { return idProducto; }
    public String getCodigoUnico() { return codigoUnico; }
    public String getNombre() { return nombre; }
    public Categoria getCategoria() { return categoria; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public EstadoRegistro getEstado() { return estado; }
}
