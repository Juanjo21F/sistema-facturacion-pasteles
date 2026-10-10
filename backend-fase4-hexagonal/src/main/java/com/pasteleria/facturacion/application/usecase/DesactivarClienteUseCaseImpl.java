package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.application.service.AuditoriaService;
import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.domain.port.input.DesactivarClienteUseCase;
import com.pasteleria.facturacion.domain.port.output.ClienteRepositoryPort;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DesactivarClienteUseCaseImpl implements DesactivarClienteUseCase {

    private final ClienteRepositoryPort clientes;
    private final AuditoriaService auditoria;

    public DesactivarClienteUseCaseImpl(ClienteRepositoryPort clientes, AuditoriaService auditoria) {
        this.clientes = clientes;
        this.auditoria = auditoria;
    }

    @Override
    public void ejecutar(Long idCliente, UsuarioAutenticado usuario) {
        Cliente cliente = clientes.buscarPorId(idCliente).orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
        cliente.desactivar();
        clientes.guardar(cliente);
        auditoria.registrar(usuario, "DESACTIVAR_CLIENTE", "documento=" + cliente.getDocumentoIdentidad());
    }
}
