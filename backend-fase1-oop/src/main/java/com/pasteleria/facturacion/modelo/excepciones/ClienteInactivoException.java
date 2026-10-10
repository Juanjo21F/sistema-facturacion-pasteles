package com.pasteleria.facturacion.modelo.excepciones;

public class ClienteInactivoException extends ReglaDeNegocioException {

    public ClienteInactivoException(String cliente) {
        super("El cliente \"" + cliente + "\" está inactivo y no puede usarse en nuevas ventas");
    }
}
