package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;

public interface AnularFacturaUseCase {

    Factura ejecutar(Long idFactura, UsuarioAutenticado usuario);
}
