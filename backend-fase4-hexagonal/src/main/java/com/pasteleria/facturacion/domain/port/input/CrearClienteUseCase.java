package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.port.input.command.DatosClienteCommand;

public interface CrearClienteUseCase {

    Cliente ejecutar(DatosClienteCommand comando);
}
