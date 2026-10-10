package com.pasteleria.facturacion.domain.entity;

public class Categoria {

    private final Long idCategoria;
    private final String nombre;
    private final String descripcion;

    public Categoria(Long idCategoria, String nombre, String descripcion) {
        this.idCategoria = idCategoria;
        this.nombre = Validaciones.textoObligatorio(nombre, "nombre de la categoría");
        this.descripcion = descripcion;
    }

    public Long getIdCategoria() { return idCategoria; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
