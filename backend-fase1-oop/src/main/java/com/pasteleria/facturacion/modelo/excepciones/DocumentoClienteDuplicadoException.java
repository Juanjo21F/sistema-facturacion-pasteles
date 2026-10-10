package com.pasteleria.facturacion.modelo.excepciones;

public class DocumentoClienteDuplicadoException extends RecursoDuplicadoException {

    public DocumentoClienteDuplicadoException(String documento) {
        super("Ya existe un cliente con el documento " + documento);
    }
}
