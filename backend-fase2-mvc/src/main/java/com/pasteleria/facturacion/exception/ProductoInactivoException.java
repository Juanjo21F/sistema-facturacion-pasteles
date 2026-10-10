package com.pasteleria.facturacion.exception;

public class ProductoInactivoException extends ReglaDeNegocioException {

    public ProductoInactivoException(String producto) {
        super("El producto \"" + producto + "\" está inactivo y no puede usarse en nuevas ventas");
    }
}
