package com.pasteleria.facturacion.domain.entity;

import com.pasteleria.facturacion.domain.exception.ClienteInactivoException;
import com.pasteleria.facturacion.domain.valueobject.EstadoRegistro;

public class Cliente {

    private final Long idCliente;
    private String documentoIdentidad;
    private String nombreCompleto;
    private String telefono;
    private String correo;
    private EstadoRegistro estado;

    private Cliente(Long idCliente, String documento, String nombre, String telefono,
                    String correo, EstadoRegistro estado) {
        this.idCliente = idCliente;
        this.estado = estado;
        aplicarDatos(documento, nombre, telefono, correo);
    }

    public static Cliente crear(String documento, String nombre, String telefono, String correo) {
        return new Cliente(null, documento, nombre, telefono, correo, EstadoRegistro.ACTIVO);
    }

    /** Reconstruye un cliente existente (usado por los adaptadores de persistencia). */
    public static Cliente reconstruir(Long id, String documento, String nombre, String telefono,
                                      String correo, EstadoRegistro estado) {
        return new Cliente(id, documento, nombre, telefono, correo, estado);
    }

    public void actualizar(String documento, String nombre, String telefono, String correo) {
        aplicarDatos(documento, nombre, telefono, correo);
    }

    public void desactivar() {
        this.estado = EstadoRegistro.INACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoRegistro.ACTIVO;
    }

    public void validarActivo() {
        if (!estaActivo()) {
            throw new ClienteInactivoException(nombreCompleto);
        }
    }

    private void aplicarDatos(String documento, String nombre, String telefono, String correo) {
        String doc = Validaciones.textoObligatorio(documento, "documentoIdentidad");
        String nom = Validaciones.textoObligatorio(nombre, "nombreCompleto");
        String tel = Validaciones.textoObligatorio(telefono, "telefono");
        String mail = Validaciones.textoObligatorio(correo, "correo");
        this.documentoIdentidad = doc;
        this.nombreCompleto = nom;
        this.telefono = tel;
        this.correo = mail;
    }

    public Long getIdCliente() { return idCliente; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public EstadoRegistro getEstado() { return estado; }
}
