package com.pasteleria.facturacion.model.repository;

import com.pasteleria.facturacion.model.entity.Producto;
import com.pasteleria.facturacion.model.enums.EstadoRegistro;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Todas las consultas traen la categoría con JOIN FETCH para evitar el problema N+1. */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("select p from Producto p join fetch p.categoria order by p.nombre")
    List<Producto> findAllConCategoria();

    @Query("select p from Producto p join fetch p.categoria where p.estado = :estado order by p.nombre")
    List<Producto> findByEstadoConCategoria(@Param("estado") EstadoRegistro estado);

    @Query("select p from Producto p join fetch p.categoria where p.idProducto = :id")
    Optional<Producto> findByIdConCategoria(@Param("id") Long id);

    @Query("select p from Producto p join fetch p.categoria where p.codigoUnico = :codigo")
    Optional<Producto> findByCodigoConCategoria(@Param("codigo") String codigo);

    /** SELECT ... FOR UPDATE, ordenado por id para evitar deadlocks entre ventas concurrentes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p join fetch p.categoria where p.idProducto in :ids order by p.idProducto")
    List<Producto> findByIdInParaActualizar(@Param("ids") Collection<Long> ids);
}
