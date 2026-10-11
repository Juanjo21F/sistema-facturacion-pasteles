package com.pasteleria.facturacion.infrastructure.adapter.out.persistence;

import com.pasteleria.facturacion.application.port.out.ClienteRepositoryPort;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.ClienteEntity;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository.ClienteRepository;
import com.pasteleria.facturacion.domain.model.Cliente;
import com.pasteleria.facturacion.domain.model.EstadoRegistro;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Adaptador de salida: implementa el puerto de clientes con Spring Data JPA. */
@Component
public class ClientePersistenceAdapter implements ClienteRepositoryPort {

    private final ClienteRepository clientes;

    public ClientePersistenceAdapter(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        return PersistenceMapper.aDominio(clientes.saveAndFlush(PersistenceMapper.aEntidad(cliente)));
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return clientes.findById(id).map(PersistenceMapper::aDominio);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documentoIdentidad) {
        return clientes.findByDocumentoIdentidad(documentoIdentidad).map(PersistenceMapper::aDominio);
    }

    @Override
    public List<Cliente> listar(boolean soloActivos) {
        List<ClienteEntity> lista = soloActivos
                ? clientes.findByEstadoOrderByNombreCompleto(EstadoRegistro.ACTIVO)
                : clientes.findAllByOrderByNombreCompleto();
        return lista.stream().map(PersistenceMapper::aDominio).toList();
    }
}
