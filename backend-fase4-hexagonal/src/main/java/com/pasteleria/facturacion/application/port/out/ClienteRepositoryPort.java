package com.pasteleria.facturacion.application.port.out;

import com.pasteleria.facturacion.domain.model.Cliente;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: persistencia de clientes. */
public interface ClienteRepositoryPort {

    Cliente guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    Optional<Cliente> buscarPorDocumento(String documentoIdentidad);

    List<Cliente> listar(boolean soloActivos);
}
