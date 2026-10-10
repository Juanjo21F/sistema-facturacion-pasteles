package com.pasteleria.facturacion.repositorios;

import com.pasteleria.facturacion.modelo.entidades.Categoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {
}
