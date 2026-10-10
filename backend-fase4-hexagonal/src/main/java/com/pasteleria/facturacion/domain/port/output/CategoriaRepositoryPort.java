package com.pasteleria.facturacion.domain.port.output;

import com.pasteleria.facturacion.domain.entity.Categoria;

import java.util.Optional;

public interface CategoriaRepositoryPort {

    Optional<Categoria> buscarPorId(Long id);
}
