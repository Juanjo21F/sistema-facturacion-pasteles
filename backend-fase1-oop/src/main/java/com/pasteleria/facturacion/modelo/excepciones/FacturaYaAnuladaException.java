package com.pasteleria.facturacion.modelo.excepciones;

public class FacturaYaAnuladaException extends ReglaDeNegocioException {

    public FacturaYaAnuladaException(String numero) {
        super("La factura " + numero + " ya fue anulada");
    }
}
