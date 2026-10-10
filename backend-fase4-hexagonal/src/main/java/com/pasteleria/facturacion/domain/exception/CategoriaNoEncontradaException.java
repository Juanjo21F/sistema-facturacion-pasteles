package com.pasteleria.facturacion.domain.exception;

public class CategoriaNoEncontradaException extends RecursoNoEncontradoException {

    public CategoriaNoEncontradaException(Long id) {
        super("No existe la categoría con id " + id);
    }
}
