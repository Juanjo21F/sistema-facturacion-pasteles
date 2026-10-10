package com.pasteleria.facturacion.exception;

public class DocumentoClienteDuplicadoException extends RecursoDuplicadoException {

    public DocumentoClienteDuplicadoException(String documento) {
        super("Ya existe un cliente con el documento " + documento);
    }
}
