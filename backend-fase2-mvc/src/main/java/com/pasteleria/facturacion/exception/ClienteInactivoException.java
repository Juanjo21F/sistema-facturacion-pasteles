package com.pasteleria.facturacion.exception;

public class ClienteInactivoException extends ReglaDeNegocioException {

    public ClienteInactivoException(String cliente) {
        super("El cliente \"" + cliente + "\" está inactivo y no puede usarse en nuevas ventas");
    }
}
