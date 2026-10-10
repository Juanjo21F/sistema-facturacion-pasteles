package com.pasteleria.facturacion.modelo.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/** Clase abstracta con los datos comunes de cualquier persona del sistema. Hoy solo hereda Cliente. */
@MappedSuperclass
public abstract class Persona extends EntidadActivable {

    @Column(name = "documento_identidad", nullable = false, unique = true, length = 30)
    private String documentoIdentidad;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    /** Las subclases dicen cómo se presenta la persona (polimorfismo). */
    public abstract String descripcion();

    protected void aplicarDatosPersona(String documento, String nombre, String telefono, String correo) {
        String doc = Validaciones.textoObligatorio(documento, "documentoIdentidad");
        String nom = Validaciones.textoObligatorio(nombre, "nombreCompleto");
        String tel = Validaciones.textoObligatorio(telefono, "telefono");
        String mail = Validaciones.textoObligatorio(correo, "correo");
        this.documentoIdentidad = doc;
        this.nombreCompleto = nom;
        this.telefono = tel;
        this.correo = mail;
    }

    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
}
