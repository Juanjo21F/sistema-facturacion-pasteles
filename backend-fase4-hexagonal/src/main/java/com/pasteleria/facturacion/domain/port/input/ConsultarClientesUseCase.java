package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.entity.Cliente;

import java.util.List;

public interface ConsultarClientesUseCase {

    List<Cliente> listar(boolean soloActivos);

    Cliente obtenerPorId(Long idCliente);
}
