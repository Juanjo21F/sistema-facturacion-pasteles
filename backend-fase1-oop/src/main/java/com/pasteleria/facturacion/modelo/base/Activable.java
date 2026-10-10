package com.pasteleria.facturacion.modelo.base;

/** Contrato de todo objeto que se puede activar/desactivar (en vez de borrarse). */
public interface Activable {

    void desactivar();

    boolean estaActivo();
}
