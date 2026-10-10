package com.pasteleria.facturacion.infrastructure.persistence.repository;

import com.pasteleria.facturacion.infrastructure.persistence.entity.CategoriaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, Long> {
}
