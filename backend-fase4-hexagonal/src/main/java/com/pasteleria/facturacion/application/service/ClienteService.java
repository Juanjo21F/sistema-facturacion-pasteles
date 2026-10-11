package com.pasteleria.facturacion.application.service;

import com.pasteleria.facturacion.application.port.in.DatosClienteCommand;
import com.pasteleria.facturacion.application.port.in.GestionarClientesUseCase;
import com.pasteleria.facturacion.application.port.out.AuditoriaPort;
import com.pasteleria.facturacion.application.port.out.ClienteRepositoryPort;
import com.pasteleria.facturacion.domain.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.domain.exception.DocumentoClienteDuplicadoException;
import com.pasteleria.facturacion.domain.model.Cliente;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;
import com.pasteleria.facturacion.domain.validation.Validaciones;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/** Aplicación: orquesta el dominio de clientes a través de puertos. */
@Transactional
public class ClienteService implements GestionarClientesUseCase {

    private final ClienteRepositoryPort clientes;
    private final AuditoriaPort auditoria;

    public ClienteService(ClienteRepositoryPort clientes, AuditoriaPort auditoria) {
        this.clientes = clientes;
        this.auditoria = auditoria;
    }

    @Override
    public Cliente crear(DatosClienteCommand datos) {
        Validaciones.textoObligatorio(datos.documentoIdentidad(), "documentoIdentidad");
        clientes.buscarPorDocumento(datos.documentoIdentidad().trim()).ifPresent(existente -> {
            throw new DocumentoClienteDuplicadoException(datos.documentoIdentidad());
        });
        Cliente cliente = Cliente.crear(datos.documentoIdentidad(), datos.nombreCompleto(), datos.telefono(), datos.correo());
        return clientes.guardar(cliente);
    }

    @Override
    public Cliente actualizar(Long idCliente, DatosClienteCommand datos) {
        Cliente cliente = buscar(idCliente);
        Validaciones.textoObligatorio(datos.documentoIdentidad(), "documentoIdentidad");
        clientes.buscarPorDocumento(datos.documentoIdentidad().trim())
                .filter(otro -> !otro.getId().equals(idCliente))
                .ifPresent(otro -> {
                    throw new DocumentoClienteDuplicadoException(datos.documentoIdentidad());
                });
        cliente.actualizarDatos(datos.documentoIdentidad(), datos.nombreCompleto(), datos.telefono(), datos.correo());
        return clientes.guardar(cliente);
    }

    @Override
    public void desactivar(Long idCliente, UsuarioAutenticado usuario) {
        Cliente cliente = buscar(idCliente);
        cliente.desactivar();
        clientes.guardar(cliente);
        auditoria.registrar(usuario, "DESACTIVAR_CLIENTE", "documento=" + cliente.getDocumentoIdentidad());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listar(boolean soloActivos) {
        return clientes.listar(soloActivos);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long idCliente) {
        return buscar(idCliente);
    }

    private Cliente buscar(Long idCliente) {
        return clientes.buscarPorId(idCliente).orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
    }
}
