package com.pasteleria.facturacion.exception;

public class CodigoProductoDuplicadoException extends RecursoDuplicadoException {

    public CodigoProductoDuplicadoException(String codigo) {
        super("Ya existe un producto con el código " + codigo);
    }
}
