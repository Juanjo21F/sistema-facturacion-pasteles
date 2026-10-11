package com.pasteleria.facturacion.infrastructure.adapter.out.persistence.repository;

import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.FacturaEntity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacturaRepository extends JpaRepository<FacturaEntity, Long> {

    @Query("""
            select f from FacturaEntity f
            join fetch f.cliente
            left join fetch f.detalles d
            left join fetch d.producto p
            left join fetch p.categoria
            where f.idFactura = :id
            """)
    Optional<FacturaEntity> findByIdConDetalles(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FacturaEntity f where f.idFactura = :id")
    Optional<FacturaEntity> findByIdParaActualizar(@Param("id") Long id);

    @Query(value = "select nextval('seq_numero_factura')", nativeQuery = true)
    Long siguienteSecuencia();
}
