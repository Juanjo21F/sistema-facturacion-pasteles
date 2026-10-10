package com.pasteleria.facturacion.modelo.reglas;

/**
 * Abstracción: una regla que debe cumplirse antes de emitir una factura. Cada regla concreta la
 * implementa a su manera y el servicio las trata a todas por igual (polimorfismo).
 */
public interface ReglaDeVenta {

    void validar(ContextoVenta contexto);
}
