package com.pasteleria.facturacion.application.port.out;

import com.pasteleria.facturacion.domain.model.Factura;

import java.util.Optional;

/** Puerto de salida: persistencia de facturas. */
public interface FacturaRepositoryPort {

    Factura guardar(Factura factura);

    Optional<Factura> buscarPorId(Long id);

    /** Bloquea la factura (lectura para actualizar): evita doble restauración de stock en anulaciones concurrentes. */
    Optional<Factura> buscarPorIdParaActualizar(Long id);

    /** Siguiente valor de la secuencia de numeración. */
    long siguienteSecuencia();
}
