package com.pasteleria.facturacion.servicios;

import com.pasteleria.facturacion.modelo.base.EntidadBase;

import java.util.Optional;

/** Clase abstracta base de los servicios de entidades: obtener-o-fallar y guardar son comunes. */
public abstract class ServicioBase<T extends EntidadBase> {

    protected abstract Optional<T> buscar(Long id);

    protected abstract RuntimeException noEncontrado(Long id);

    protected abstract T guardar(T entidad);

    public T obtenerPorId(Long id) {
        return buscar(id).orElseThrow(() -> noEncontrado(id));
    }
}
