package com.pasteleria.facturacion.model.entity;

import com.pasteleria.facturacion.exception.ClienteInactivoException;
import com.pasteleria.facturacion.model.enums.EstadoRegistro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Modelo: cliente. La entidad conoce y protege sus propias reglas. */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "documento_identidad", nullable = false, unique = true, length = 30)
    private String documentoIdentidad;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoRegistro estado;

    protected Cliente() {
    }

    private Cliente(String documento, String nombre, String telefono, String correo) {
        this.estado = EstadoRegistro.ACTIVO;
        aplicarDatos(documento, nombre, telefono, correo);
    }

    public static Cliente crear(String documento, String nombre, String telefono, String correo) {
        return new Cliente(documento, nombre, telefono, correo);
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
