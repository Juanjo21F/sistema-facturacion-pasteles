package com.pasteleria.facturacion.domain.exception;

public class RecursoDuplicadoException extends ReglaDeNegocioException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
