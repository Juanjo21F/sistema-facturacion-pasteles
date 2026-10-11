package com.pasteleria.facturacion.domain.model;

import com.pasteleria.facturacion.domain.exception.ClienteInactivoException;
import com.pasteleria.facturacion.domain.validation.Validaciones;

/** Dominio: cliente. Sus reglas (datos obligatorios, estado) viven aquí. */
public class Cliente {

    private Long id;
    private String documentoIdentidad;
    private String nombreCompleto;
    private String telefono;
    private String correo;
    private EstadoRegistro estado;

    public Cliente(Long id, String documentoIdentidad, String nombreCompleto, String telefono, String correo,
                   EstadoRegistro estado) {
        this.id = id;
        this.documentoIdentidad = documentoIdentidad;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.correo = correo;
        this.estado = estado;
    }

    public static Cliente crear(String documentoIdentidad, String nombreCompleto, String telefono, String correo) {
        Cliente cliente = new Cliente(null, null, null, null, null, EstadoRegistro.ACTIVO);
        cliente.actualizarDatos(documentoIdentidad, nombreCompleto, telefono, correo);
        return cliente;
    }

    public void actualizarDatos(String documentoIdentidad, String nombreCompleto, String telefono, String correo) {
        this.documentoIdentidad = Validaciones.textoObligatorio(documentoIdentidad, "documentoIdentidad");
        this.nombreCompleto = Validaciones.textoObligatorio(nombreCompleto, "nombreCompleto");
        this.telefono = Validaciones.textoObligatorio(telefono, "telefono");
        this.correo = Validaciones.textoObligatorio(correo, "correo");
    }

    public void desactivar() {
        this.estado = EstadoRegistro.INACTIVO;
    }

    public void validarActivoParaVenta() {
        if (estado != EstadoRegistro.ACTIVO) {
            throw new ClienteInactivoException(nombreCompleto);
        }
    }

    public Long getId() { return id; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public EstadoRegistro getEstado() { return estado; }
}
