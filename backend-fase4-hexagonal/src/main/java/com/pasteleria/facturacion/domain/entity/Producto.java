package com.pasteleria.facturacion.domain.entity;

import com.pasteleria.facturacion.domain.exception.ProductoInactivoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.exception.StockInsuficienteException;
import com.pasteleria.facturacion.domain.valueobject.EstadoRegistro;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Producto {

    private final Long idProducto;
    private String codigoUnico;
    private String nombre;
    private Categoria categoria;
    private BigDecimal precio;
    private int stock;
    private EstadoRegistro estado;

    private Producto(Long idProducto, String codigo, String nombre, Categoria categoria,
                     BigDecimal precio, int stock, EstadoRegistro estado) {
        this.idProducto = idProducto;
        this.estado = estado;
        aplicarDatos(codigo, nombre, categoria, precio, stock);
    }

    public static Producto crear(String codigo, String nombre, Categoria categoria,
                                 BigDecimal precio, int stock) {
        return new Producto(null, codigo, nombre, categoria, precio, stock, EstadoRegistro.ACTIVO);
    }

    /** Reconstruye un producto existente (usado por los adaptadores de persistencia). */
    public static Producto reconstruir(Long id, String codigo, String nombre, Categoria categoria,
                                       BigDecimal precio, int stock, EstadoRegistro estado) {
        return new Producto(id, codigo, nombre, categoria, precio, stock, estado);
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
