package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.exception.DocumentoClienteDuplicadoException;
import com.pasteleria.facturacion.domain.port.input.CrearClienteUseCase;
import com.pasteleria.facturacion.domain.port.input.command.DatosClienteCommand;
import com.pasteleria.facturacion.domain.port.output.ClienteRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CrearClienteUseCaseImpl implements CrearClienteUseCase {

    private final ClienteRepositoryPort clientes;

    public CrearClienteUseCaseImpl(ClienteRepositoryPort clientes) {
        this.clientes = clientes;
    }

    @Override
    public Cliente ejecutar(DatosClienteCommand comando) {
        clientes.buscarPorDocumento(comando.documentoIdentidad()).ifPresent(existente -> {
            throw new DocumentoClienteDuplicadoException(comando.documentoIdentidad());
        });
        Cliente cliente = Cliente.crear(comando.documentoIdentidad(), comando.nombreCompleto(),
                comando.telefono(), comando.correo());
        return clientes.guardar(cliente);
    }
}
