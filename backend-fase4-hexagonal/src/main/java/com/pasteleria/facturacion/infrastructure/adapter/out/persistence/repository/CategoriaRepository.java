package com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository;

import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.CategoriaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {
}
