package com.pasteleria.facturacion.modelo.excepciones;

public class StockInsuficienteException extends ReglaDeNegocioException {

    public StockInsuficienteException(String producto, int solicitado, int disponible) {
        super("Stock insuficiente para el producto \"" + producto + "\": solicitado=" + solicitado + ", disponible=" + disponible);
    }
}
