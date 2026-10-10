package com.pasteleria.facturacion.modelo.entidades;

import com.pasteleria.facturacion.modelo.base.EntidadActivable;
import com.pasteleria.facturacion.modelo.base.Validaciones;
import com.pasteleria.facturacion.modelo.base.Vendible;
import com.pasteleria.facturacion.modelo.excepciones.ProductoInactivoException;
import com.pasteleria.facturacion.modelo.excepciones.ReglaDeNegocioException;
import com.pasteleria.facturacion.modelo.excepciones.StockInsuficienteException;

import jakarta.persistence.AttributeOverride;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Producto → EntidadActivable → EntidadBase, e implementa Vendible. */
@Entity
@Table(name = "productos")
@AttributeOverride(name = "id", column = @Column(name = "id_producto"))
public class Producto extends EntidadActivable implements Vendible {

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

    protected Producto() {
    }

    private Producto(String codigo, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        aplicarDatos(codigo, nombre, categoria, precio, stock);
    }

    public static Producto crear(String codigo, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        return new Producto(codigo, nombre, categoria, precio, stock);
    }

    public void actualizar(String codigo, String nombre, Categoria categoria, BigDecimal precio, int stock) {
        aplicarDatos(codigo, nombre, categoria, precio, stock);
    }

    @Override
    public void validarDisponibleParaVenta() {
        if (!estaActivo()) {
            throw new ProductoInactivoException(nombre);
        }
    }

    @Override
    public boolean tieneStockPara(int cantidad) {
        return cantidad <= stock;
    }

    @Override
    public void descontarStock(int cantidad) {
        Validaciones.cantidadPositiva(cantidad);
        if (!tieneStockPara(cantidad)) {
            throw new StockInsuficienteException(nombre, cantidad, stock);
        }
        this.stock -= cantidad;
    }

    @Override
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

    public String getCodigoUnico() { return codigoUnico; }
    public String getNombre() { return nombre; }
    public Categoria getCategoria() { return categoria; }
    @Override public BigDecimal getPrecio() { return precio; }
    @Override public int getStock() { return stock; }
}
