package com.pasteleria.facturacion.application.port.in;

import com.pasteleria.facturacion.domain.model.Cliente;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

import java.util.List;

/** Puerto de entrada: casos de uso de clientes. */
public interface GestionarClientesUseCase {

    Cliente crear(DatosClienteCommand datos);

    Cliente actualizar(Long idCliente, DatosClienteCommand datos);

    void desactivar(Long idCliente, UsuarioAutenticado usuario);

    List<Cliente> listar(boolean soloActivos);

    Cliente obtenerPorId(Long idCliente);
}
