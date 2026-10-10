package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.port.input.command.DatosProductoCommand;

public interface CrearProductoUseCase {

    Producto ejecutar(DatosProductoCommand comando);
}
