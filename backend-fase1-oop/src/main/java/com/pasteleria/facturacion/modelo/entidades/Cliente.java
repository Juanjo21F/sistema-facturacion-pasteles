package com.pasteleria.facturacion.modelo.entidades;

import com.pasteleria.facturacion.modelo.base.Persona;
import com.pasteleria.facturacion.modelo.excepciones.ClienteInactivoException;

import jakarta.persistence.AttributeOverride;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Cliente → Persona → EntidadActivable → EntidadBase. */
@Entity
@Table(name = "clientes")
@AttributeOverride(name = "id", column = @Column(name = "id_cliente"))
public class Cliente extends Persona {

    protected Cliente() {
    }

    private Cliente(String documento, String nombre, String telefono, String correo) {
        aplicarDatosPersona(documento, nombre, telefono, correo);
    }

    public static Cliente crear(String documento, String nombre, String telefono, String correo) {
        return new Cliente(documento, nombre, telefono, correo);
    }

    public void actualizar(String documento, String nombre, String telefono, String correo) {
        aplicarDatosPersona(documento, nombre, telefono, correo);
    }

    public void validarActivo() {
        if (!estaActivo()) {
            throw new ClienteInactivoException(getNombreCompleto());
        }
    }

    @Override
    public String descripcion() {
        return "Cliente " + getNombreCompleto() + " (doc. " + getDocumentoIdentidad() + ")";
    }
}
