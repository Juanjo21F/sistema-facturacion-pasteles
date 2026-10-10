package com.pasteleria.facturacion.model.service;

import com.pasteleria.facturacion.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.exception.DocumentoClienteDuplicadoException;
import com.pasteleria.facturacion.model.entity.Cliente;
import com.pasteleria.facturacion.model.enums.EstadoRegistro;
import com.pasteleria.facturacion.model.repository.ClienteRepository;
import com.pasteleria.facturacion.model.security.UsuarioAutenticado;
import com.pasteleria.facturacion.model.service.command.DatosClienteCommand;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Modelo: casos de uso de clientes. */
@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clientes;
    private final AuditoriaService auditoria;

    public ClienteService(ClienteRepository clientes, AuditoriaService auditoria) {
        this.clientes = clientes;
        this.auditoria = auditoria;
    }

    public Cliente crear(DatosClienteCommand datos) {
        clientes.findByDocumentoIdentidad(datos.documentoIdentidad()).ifPresent(existente -> {
            throw new DocumentoClienteDuplicadoException(datos.documentoIdentidad());
        });
        Cliente cliente = Cliente.crear(datos.documentoIdentidad(), datos.nombreCompleto(),
                datos.telefono(), datos.correo());
        return clientes.saveAndFlush(cliente);
    }

    public Cliente actualizar(Long idCliente, DatosClienteCommand datos) {
        Cliente cliente = obtenerPorId(idCliente);
        clientes.findByDocumentoIdentidad(datos.documentoIdentidad())
                .filter(otro -> !otro.getIdCliente().equals(idCliente))
                .ifPresent(otro -> {
                    throw new DocumentoClienteDuplicadoException(datos.documentoIdentidad());
                });
        cliente.actualizar(datos.documentoIdentidad(), datos.nombreCompleto(), datos.telefono(), datos.correo());
        return clientes.saveAndFlush(cliente);
    }

    public void desactivar(Long idCliente, UsuarioAutenticado usuario) {
        Cliente cliente = obtenerPorId(idCliente);
        cliente.desactivar();
        clientes.saveAndFlush(cliente);
        auditoria.registrar(usuario, "DESACTIVAR_CLIENTE", "documento=" + cliente.getDocumentoIdentidad());
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(boolean soloActivos) {
        return soloActivos
                ? clientes.findByEstadoOrderByNombreCompleto(EstadoRegistro.ACTIVO)
                : clientes.findAllByOrderByNombreCompleto();
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long idCliente) {
        return clientes.findById(idCliente).orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
    }
}
