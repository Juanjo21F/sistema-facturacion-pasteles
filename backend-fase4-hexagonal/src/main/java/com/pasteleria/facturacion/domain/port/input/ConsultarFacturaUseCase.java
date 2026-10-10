package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.entity.Factura;

public interface ConsultarFacturaUseCase {

    Factura ejecutar(Long idFactura);
}
