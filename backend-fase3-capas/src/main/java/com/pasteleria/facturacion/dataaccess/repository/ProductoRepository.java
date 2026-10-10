package com.pasteleria.facturacion.dataaccess.repository;

import com.pasteleria.facturacion.dataaccess.entity.EstadoRegistro;
import com.pasteleria.facturacion.dataaccess.entity.ProductoEntity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Todas las consultas traen la categoría con JOIN FETCH para evitar el problema N+1. */
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    @Query("select p from ProductoEntity p join fetch p.categoria order by p.nombre")
    List<ProductoEntity> findAllConCategoria();

    @Query("select p from ProductoEntity p join fetch p.categoria where p.estado = :estado order by p.nombre")
    List<ProductoEntity> findByEstadoConCategoria(@Param("estado") EstadoRegistro estado);

    @Query("select p from ProductoEntity p join fetch p.categoria where p.idProducto = :id")
    Optional<ProductoEntity> findByIdConCategoria(@Param("id") Long id);

    @Query("select p from ProductoEntity p join fetch p.categoria where p.codigoUnico = :codigo")
    Optional<ProductoEntity> findByCodigoConCategoria(@Param("codigo") String codigo);

    /** SELECT ... FOR UPDATE, ordenado por id para evitar deadlocks entre ventas concurrentes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductoEntity p join fetch p.categoria where p.idProducto in :ids order by p.idProducto")
    List<ProductoEntity> findByIdInParaActualizar(@Param("ids") Collection<Long> ids);
}
