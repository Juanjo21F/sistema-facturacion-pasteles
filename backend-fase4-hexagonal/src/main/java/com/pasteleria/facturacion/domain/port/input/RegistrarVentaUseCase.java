package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.port.input.command.RegistrarVentaCommand;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;

public interface RegistrarVentaUseCase {

    Factura ejecutar(RegistrarVentaCommand comando, UsuarioAutenticado usuario);
}
