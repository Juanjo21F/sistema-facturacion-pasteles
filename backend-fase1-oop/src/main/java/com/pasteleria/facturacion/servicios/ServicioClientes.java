package com.pasteleria.facturacion.servicios;

import com.pasteleria.facturacion.modelo.entidades.Cliente;
import com.pasteleria.facturacion.modelo.enumeraciones.EstadoRegistro;
import com.pasteleria.facturacion.modelo.excepciones.ClienteNoEncontradoException;
import com.pasteleria.facturacion.modelo.excepciones.DocumentoClienteDuplicadoException;
import com.pasteleria.facturacion.repositorios.ClienteRepositorio;
import com.pasteleria.facturacion.servicios.comandos.DatosClienteCommand;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioClientes extends ServicioActivable<Cliente> {

    private final ClienteRepositorio clientes;

    public ServicioClientes(ClienteRepositorio clientes, ServicioAuditoria auditoria) {
        super(auditoria);
        this.clientes = clientes;
    }

    public Cliente crear(DatosClienteCommand datos) {
        clientes.findByDocumentoIdentidad(datos.documentoIdentidad()).ifPresent(existente -> {
            throw new DocumentoClienteDuplicadoException(datos.documentoIdentidad());
        });
        return guardar(Cliente.crear(datos.documentoIdentidad(), datos.nombreCompleto(), datos.telefono(), datos.correo()));
    }

    public Cliente actualizar(Long idCliente, DatosClienteCommand datos) {
        Cliente cliente = obtenerPorId(idCliente);
        clientes.findByDocumentoIdentidad(datos.documentoIdentidad())
                .filter(otro -> !otro.getId().equals(idCliente))
                .ifPresent(otro -> {
                    throw new DocumentoClienteDuplicadoException(datos.documentoIdentidad());
                });
        cliente.actualizar(datos.documentoIdentidad(), datos.nombreCompleto(), datos.telefono(), datos.correo());
        return guardar(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(boolean soloActivos) {
        return soloActivos
                ? clientes.findByEstadoOrderByNombreCompleto(EstadoRegistro.ACTIVO)
                : clientes.findAllByOrderByNombreCompleto();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return super.obtenerPorId(id);
    }

    @Override
    protected Optional<Cliente> buscar(Long id) {
        return clientes.findById(id);
    }

    @Override
    protected RuntimeException noEncontrado(Long id) {
        return new ClienteNoEncontradoException(id);
    }

    @Override
    protected Cliente guardar(Cliente cliente) {
        return clientes.saveAndFlush(cliente);
    }

    @Override
    protected String operacionDesactivar() {
        return "DESACTIVAR_CLIENTE";
    }

    @Override
    protected String detalleAuditoria(Cliente cliente) {
        return "documento=" + cliente.getDocumentoIdentidad();
    }
}
