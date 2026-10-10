package com.pasteleria.facturacion.modelo.excepciones;

public class RecursoNoEncontradoException extends ReglaDeNegocioException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
