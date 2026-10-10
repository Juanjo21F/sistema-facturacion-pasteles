package com.pasteleria.facturacion.modelo.base;

import com.pasteleria.facturacion.modelo.enumeraciones.EstadoRegistro;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;

/** Segundo nivel de la jerarquía: entidades con estado ACTIVO/INACTIVO (productos, clientes). */
@MappedSuperclass
public abstract class EntidadActivable extends EntidadBase implements Activable {

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoRegistro estado = EstadoRegistro.ACTIVO;

    @Override
    public void desactivar() {
        this.estado = EstadoRegistro.INACTIVO;
    }

    @Override
    public boolean estaActivo() {
        return estado == EstadoRegistro.ACTIVO;
    }

    public EstadoRegistro getEstado() {
        return estado;
    }
}
