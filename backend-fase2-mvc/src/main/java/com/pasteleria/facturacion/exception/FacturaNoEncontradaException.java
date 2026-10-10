package com.pasteleria.facturacion.exception;

public class FacturaNoEncontradaException extends RecursoNoEncontradoException {

    public FacturaNoEncontradaException(Long id) {
        super("No existe la factura con id " + id);
    }
}
