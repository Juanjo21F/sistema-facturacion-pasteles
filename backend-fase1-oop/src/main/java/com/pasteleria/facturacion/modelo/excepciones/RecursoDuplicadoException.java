package com.pasteleria.facturacion.modelo.excepciones;

public class RecursoDuplicadoException extends ReglaDeNegocioException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
