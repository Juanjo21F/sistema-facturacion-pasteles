package com.pasteleria.facturacion.infrastructure.adapter.out.persistence;

import com.pasteleria.facturacion.application.port.out.CategoriaRepositoryPort;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository.CategoriaRepository;
import com.pasteleria.facturacion.domain.model.Categoria;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CategoriaPersistenceAdapter implements CategoriaRepositoryPort {

    private final CategoriaRepository repositorio;

    public CategoriaPersistenceAdapter(CategoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return repositorio.findById(id).map(PersistenceMapper::aDominio);
    }
}
