package com.pasteleria.facturacion.domain.exception;

public class DocumentoClienteDuplicadoException extends RecursoDuplicadoException {

    public DocumentoClienteDuplicadoException(String documento) {
        super("Ya existe un cliente con el documento " + documento);
    }
}
