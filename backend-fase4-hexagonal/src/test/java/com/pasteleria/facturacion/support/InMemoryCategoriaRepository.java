package com.pasteleria.facturacion.support;

import com.pasteleria.facturacion.domain.entity.Categoria;
import com.pasteleria.facturacion.domain.port.output.CategoriaRepositoryPort;

import java.util.Optional;

public class InMemoryCategoriaRepository implements CategoriaRepositoryPort {

    public static final Categoria TORTAS = new Categoria(1L, "Tortas", "Tortas grandes");

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return TORTAS.getIdCategoria().equals(id) ? Optional.of(TORTAS) : Optional.empty();
    }
}
