package com.pasteleria.facturacion.business.exception;

public class FacturaYaAnuladaException extends ReglaDeNegocioException {

    public FacturaYaAnuladaException(String numero) {
        super("La factura " + numero + " ya fue anulada");
    }
}
