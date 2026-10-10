package com.pasteleria.facturacion.exception;

public class ClienteNoEncontradoException extends RecursoNoEncontradoException {

    public ClienteNoEncontradoException(Long id) {
        super("No existe el cliente con id " + id);
    }
}
