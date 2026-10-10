package com.pasteleria.facturacion.infrastructure.persistence.adapter;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.domain.port.output.ClienteRepositoryPort;
import com.pasteleria.facturacion.domain.valueobject.EstadoRegistro;
import com.pasteleria.facturacion.infrastructure.persistence.entity.ClienteJpaEntity;
import com.pasteleria.facturacion.infrastructure.persistence.repository.ClienteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository jpa;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        ClienteJpaEntity entidad = cliente.getIdCliente() == null
                ? new ClienteJpaEntity()
                : jpa.findById(cliente.getIdCliente())
                        .orElseThrow(() -> new ClienteNoEncontradoException(cliente.getIdCliente()));
        entidad.setDocumentoIdentidad(cliente.getDocumentoIdentidad());
        entidad.setNombreCompleto(cliente.getNombreCompleto());
        entidad.setTelefono(cliente.getTelefono());
        entidad.setCorreo(cliente.getCorreo());
        entidad.setEstado(cliente.getEstado());
        ClienteJpaEntity guardado = jpa.saveAndFlush(entidad);
        return PersistenceMapper.aDominio(guardado);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMapper::aDominio);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documentoIdentidad) {
        return jpa.findByDocumentoIdentidad(documentoIdentidad).map(PersistenceMapper::aDominio);
    }

    @Override
    public List<Cliente> buscarTodos(boolean soloActivos) {
        List<ClienteJpaEntity> entidades = soloActivos
                ? jpa.findByEstadoOrderByNombreCompleto(EstadoRegistro.ACTIVO)
                : jpa.findAllByOrderByNombreCompleto();
        return entidades.stream().map(PersistenceMapper::aDominio).toList();
    }
}
