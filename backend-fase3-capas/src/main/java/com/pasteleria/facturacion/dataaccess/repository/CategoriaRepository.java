package com.pasteleria.facturacion.dataaccess.repository;

import com.pasteleria.facturacion.dataaccess.entity.CategoriaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {
}
