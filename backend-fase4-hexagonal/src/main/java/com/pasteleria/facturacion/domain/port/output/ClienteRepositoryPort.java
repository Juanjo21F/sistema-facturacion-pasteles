package com.pasteleria.facturacion.domain.port.output;

import com.pasteleria.facturacion.domain.entity.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {

    Cliente guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    Optional<Cliente> buscarPorDocumento(String documentoIdentidad);

    List<Cliente> buscarTodos(boolean soloActivos);
}
