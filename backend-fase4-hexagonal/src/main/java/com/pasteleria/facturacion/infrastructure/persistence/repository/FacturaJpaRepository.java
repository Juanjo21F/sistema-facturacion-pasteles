package com.pasteleria.facturacion.infrastructure.persistence.repository;

import com.pasteleria.facturacion.infrastructure.persistence.entity.FacturaJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacturaJpaRepository extends JpaRepository<FacturaJpaEntity, Long> {

    @Query("""
            select f from FacturaJpaEntity f
            join fetch f.cliente
            left join fetch f.detalles d
            left join fetch d.producto p
            left join fetch p.categoria
            where f.id = :id
            """)
    Optional<FacturaJpaEntity> findByIdConDetalles(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FacturaJpaEntity f where f.id = :id")
    Optional<FacturaJpaEntity> findByIdParaActualizar(@Param("id") Long id);

    @Query(value = "select nextval('seq_numero_factura')", nativeQuery = true)
    Long siguienteSecuencia();
}
