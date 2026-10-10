package com.pasteleria.facturacion.infrastructure.persistence.adapter;

import com.pasteleria.facturacion.domain.entity.Categoria;
import com.pasteleria.facturacion.domain.port.output.CategoriaRepositoryPort;
import com.pasteleria.facturacion.infrastructure.persistence.repository.CategoriaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CategoriaRepositoryAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository jpa;

    public CategoriaRepositoryAdapter(CategoriaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMapper::aDominio);
    }
}
