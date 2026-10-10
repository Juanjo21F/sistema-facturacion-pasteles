package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.port.input.command.DatosClienteCommand;

public interface ActualizarClienteUseCase {

    Cliente ejecutar(Long idCliente, DatosClienteCommand comando);
}
