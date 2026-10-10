package com.pasteleria.facturacion.business.service;

import com.pasteleria.facturacion.business.dto.ClienteResponse;
import com.pasteleria.facturacion.business.dto.DatosClienteCommand;
import com.pasteleria.facturacion.business.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.business.exception.DocumentoClienteDuplicadoException;
import com.pasteleria.facturacion.business.mapper.EntityMapper;
import com.pasteleria.facturacion.business.security.UsuarioAutenticado;
import com.pasteleria.facturacion.business.validation.Validaciones;
import com.pasteleria.facturacion.dataaccess.entity.ClienteEntity;
import com.pasteleria.facturacion.dataaccess.entity.EstadoRegistro;
import com.pasteleria.facturacion.dataaccess.repository.ClienteRepository;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capa de negocio: reglas de clientes. */
@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clientes;
    private final AuditoriaService auditoria;

    public ClienteService(ClienteRepository clientes, AuditoriaService auditoria) {
        this.clientes = clientes;
        this.auditoria = auditoria;
    }

    public ClienteResponse crear(DatosClienteCommand datos) {
        validar(datos);
        clientes.findByDocumentoIdentidad(datos.documentoIdentidad().trim()).ifPresent(existente -> {
            throw new DocumentoClienteDuplicadoException(datos.documentoIdentidad());
        });
        ClienteEntity cliente = new ClienteEntity();
        cliente.setEstado(EstadoRegistro.ACTIVO);
        aplicar(cliente, datos);
        return EntityMapper.aRespuesta(clientes.saveAndFlush(cliente));
    }

    public ClienteResponse actualizar(Long idCliente, DatosClienteCommand datos) {
        ClienteEntity cliente = buscar(idCliente);
        validar(datos);
        clientes.findByDocumentoIdentidad(datos.documentoIdentidad().trim())
                .filter(otro -> !otro.getIdCliente().equals(idCliente))
                .ifPresent(otro -> {
                    throw new DocumentoClienteDuplicadoException(datos.documentoIdentidad());
                });
        aplicar(cliente, datos);
        return EntityMapper.aRespuesta(clientes.saveAndFlush(cliente));
    }

    public void desactivar(Long idCliente, UsuarioAutenticado usuario) {
        ClienteEntity cliente = buscar(idCliente);
        cliente.setEstado(EstadoRegistro.INACTIVO);
        clientes.saveAndFlush(cliente);
        auditoria.registrar(usuario, "DESACTIVAR_CLIENTE", "documento=" + cliente.getDocumentoIdentidad());
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(boolean soloActivos) {
        List<ClienteEntity> lista = soloActivos
                ? clientes.findByEstadoOrderByNombreCompleto(EstadoRegistro.ACTIVO)
                : clientes.findAllByOrderByNombreCompleto();
        return lista.stream().map(EntityMapper::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long idCliente) {
        return EntityMapper.aRespuesta(buscar(idCliente));
    }

    private void validar(DatosClienteCommand datos) {
        Validaciones.textoObligatorio(datos.documentoIdentidad(), "documentoIdentidad");
        Validaciones.textoObligatorio(datos.nombreCompleto(), "nombreCompleto");
        Validaciones.textoObligatorio(datos.telefono(), "telefono");
        Validaciones.textoObligatorio(datos.correo(), "correo");
    }

    private void aplicar(ClienteEntity cliente, DatosClienteCommand datos) {
        cliente.setDocumentoIdentidad(datos.documentoIdentidad().trim());
        cliente.setNombreCompleto(datos.nombreCompleto().trim());
        cliente.setTelefono(datos.telefono().trim());
        cliente.setCorreo(datos.correo().trim());
    }

    private ClienteEntity buscar(Long idCliente) {
        return clientes.findById(idCliente).orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
    }
}
