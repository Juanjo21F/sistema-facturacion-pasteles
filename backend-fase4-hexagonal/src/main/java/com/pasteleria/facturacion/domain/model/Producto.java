package com.pasteleria.facturacion.domain.model;

import com.pasteleria.facturacion.domain.exception.ProductoInactivoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.exception.StockInsuficienteException;
import com.pasteleria.facturacion.domain.validation.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Dominio: producto con sus reglas de inventario (validar, descontar y restaurar stock). Sin dependencias de frameworks. */
public class Producto {

    private Long id;
    private String codigoUnico;
    private String nombre;
    private Categoria categoria;
    private BigDecimal precio;
    private int stock;
    private EstadoRegistro estado;

    public Producto(Long id, String codigoUnico, String nombre, Categoria categoria, BigDecimal precio,
                    int stock, EstadoRegistro estado) {
        this.id = id;
        this.codigoUnico = codigoUnico;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.estado = estado;
    }

    /** Crea un producto nuevo (ACTIVO) validando sus reglas. */
    public static Producto crear(String codigoUnico, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        Producto producto = new Producto(null, null, null, null, null, 0, EstadoRegistro.ACTIVO);
        producto.actualizarDatos(codigoUnico, nombre, categoria, precio, stock);
        return producto;
    }

    public void actualizarDatos(String codigoUnico, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        Validaciones.textoObligatorio(codigoUnico, "codigoUnico");
        Validaciones.textoObligatorio(nombre, "nombre");
        if (categoria == null) {
            throw new ReglaDeNegocioException("La categoría es obligatoria");
        }
        Validaciones.mayorQueCero(precio, "precio");
        if (stock < 0) {
            throw new ReglaDeNegocioException("El stock no puede ser negativo");
        }
        this.codigoUnico = codigoUnico.trim();
        this.nombre = nombre.trim();
        this.categoria = categoria;
        this.precio = precio.setScale(2, RoundingMode.HALF_UP);
        this.stock = stock;
    }

    public void desactivar() {
        this.estado = EstadoRegistro.INACTIVO;
    }

    public void validarDisponibleParaVenta() {
        if (estado != EstadoRegistro.ACTIVO) {
            throw new ProductoInactivoException(nombre);
        }
    }

    public void validarStock(int cantidad) {
        Validaciones.cantidadPositiva(cantidad);
        if (cantidad > stock) {
            throw new StockInsuficienteException(nombre, cantidad, stock);
        }
    }

    public void descontar(int cantidad) {
        validarStock(cantidad);
        stock -= cantidad;
    }

    public void restaurar(int cantidad) {
        Validaciones.cantidadPositiva(cantidad);
        stock += cantidad;
    }

    public Long getId() { return id; }
    public String getCodigoUnico() { return codigoUnico; }
    public String getNombre() { return nombre; }
    public Categoria getCategoria() { return categoria; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public EstadoRegistro getEstado() { return estado; }
}
