package com.pasteleria.facturacion.application.port.out;

import com.pasteleria.facturacion.domain.model.Categoria;

import java.util.Optional;

/** Puerto de salida: consulta de categorías. */
public interface CategoriaRepositoryPort {

    Optional<Categoria> buscarPorId(Long id);
}
