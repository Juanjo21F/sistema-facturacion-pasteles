package com.pasteleria.facturacion.modelo.base;

/** Contrato de los documentos que se pueden anular una sola vez. */
public interface Anulable {

    void anular();

    boolean estaAnulada();
}
