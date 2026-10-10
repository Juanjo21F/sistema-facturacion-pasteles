package com.pasteleria.facturacion.domain.port.output;

import com.pasteleria.facturacion.domain.entity.Factura;

import java.util.Optional;

public interface FacturaRepositoryPort {

    Factura guardar(Factura factura);

    Optional<Factura> buscarPorId(Long id);

    /** Igual que buscarPorId pero bloqueando la factura: evita que dos anulaciones simultáneas restauren stock dos veces. */
    Optional<Factura> buscarPorIdConBloqueo(Long id);

    /** Siguiente valor de la secuencia única de numeración (la base de datos garantiza la unicidad). */
    long siguienteSecuencia();
}
