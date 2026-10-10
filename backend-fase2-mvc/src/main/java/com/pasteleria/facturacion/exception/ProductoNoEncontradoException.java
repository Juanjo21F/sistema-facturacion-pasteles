package com.pasteleria.facturacion.exception;

public class ProductoNoEncontradoException extends RecursoNoEncontradoException {

    public ProductoNoEncontradoException(Long id) {
        super("No existe el producto con id " + id);
    }
}
