package com.pasteleria.facturacion.infrastructure.persistence.repository;

import com.pasteleria.facturacion.domain.valueobject.EstadoRegistro;
import com.pasteleria.facturacion.infrastructure.persistence.entity.ProductoJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Todas las consultas traen la categoría con JOIN FETCH para evitar el problema N+1. */
public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, Long> {

    @Query("select p from ProductoJpaEntity p join fetch p.categoria order by p.nombre")
    List<ProductoJpaEntity> findAllConCategoria();

    @Query("select p from ProductoJpaEntity p join fetch p.categoria where p.estado = :estado order by p.nombre")
    List<ProductoJpaEntity> findByEstadoConCategoria(@Param("estado") EstadoRegistro estado);

    @Query("select p from ProductoJpaEntity p join fetch p.categoria where p.id = :id")
    Optional<ProductoJpaEntity> findByIdConCategoria(@Param("id") Long id);

    @Query("select p from ProductoJpaEntity p join fetch p.categoria where p.codigoUnico = :codigo")
    Optional<ProductoJpaEntity> findByCodigoConCategoria(@Param("codigo") String codigo);

    /** SELECT ... FOR UPDATE, ordenado por id para evitar deadlocks entre ventas concurrentes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductoJpaEntity p join fetch p.categoria where p.id in :ids order by p.id")
    List<ProductoJpaEntity> findByIdInParaActualizar(@Param("ids") Collection<Long> ids);
}
