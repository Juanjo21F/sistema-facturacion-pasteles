package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.domain.exception.DocumentoClienteDuplicadoException;
import com.pasteleria.facturacion.domain.port.input.ActualizarClienteUseCase;
import com.pasteleria.facturacion.domain.port.input.command.DatosClienteCommand;
import com.pasteleria.facturacion.domain.port.output.ClienteRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActualizarClienteUseCaseImpl implements ActualizarClienteUseCase {

    private final ClienteRepositoryPort clientes;

    public ActualizarClienteUseCaseImpl(ClienteRepositoryPort clientes) {
        this.clientes = clientes;
    }

    @Override
    public Cliente ejecutar(Long idCliente, DatosClienteCommand comando) {
        Cliente cliente = clientes.buscarPorId(idCliente).orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
        clientes.buscarPorDocumento(comando.documentoIdentidad())
                .filter(otro -> !otro.getIdCliente().equals(idCliente))
                .ifPresent(otro -> {
                    throw new DocumentoClienteDuplicadoException(comando.documentoIdentidad());
                });
        cliente.actualizar(comando.documentoIdentidad(), comando.nombreCompleto(),
                comando.telefono(), comando.correo());
        return clientes.guardar(cliente);
    }
}
