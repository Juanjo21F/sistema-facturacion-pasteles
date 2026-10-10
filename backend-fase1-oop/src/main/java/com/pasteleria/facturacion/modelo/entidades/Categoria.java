package com.pasteleria.facturacion.modelo.entidades;

import com.pasteleria.facturacion.modelo.base.EntidadBase;
import com.pasteleria.facturacion.modelo.base.Validaciones;

import jakarta.persistence.AttributeOverride;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
@AttributeOverride(name = "id", column = @Column(name = "id_categoria"))
public class Categoria extends EntidadBase {

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    protected Categoria() {
    }

    public Categoria(String nombre, String descripcion) {
        this.nombre = Validaciones.textoObligatorio(nombre, "nombre de la categoría");
        this.descripcion = descripcion;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
