package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.domain.port.input.ConsultarClientesUseCase;
import com.pasteleria.facturacion.domain.port.output.ClienteRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultarClientesUseCaseImpl implements ConsultarClientesUseCase {

    private final ClienteRepositoryPort clientes;

    public ConsultarClientesUseCaseImpl(ClienteRepositoryPort clientes) {
        this.clientes = clientes;
    }

    @Override
    public List<Cliente> listar(boolean soloActivos) {
        return clientes.buscarTodos(soloActivos);
    }

    @Override
    public Cliente obtenerPorId(Long idCliente) {
        return clientes.buscarPorId(idCliente).orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
    }
}
