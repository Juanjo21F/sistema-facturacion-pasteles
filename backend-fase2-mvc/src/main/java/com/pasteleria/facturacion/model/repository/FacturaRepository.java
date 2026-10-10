package com.pasteleria.facturacion.model.repository;

import com.pasteleria.facturacion.model.entity.Factura;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    @Query("""
            select f from Factura f
            join fetch f.cliente
            left join fetch f.detalles d
            left join fetch d.producto p
            left join fetch p.categoria
            where f.idFactura = :id
            """)
    Optional<Factura> findByIdConDetalles(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f where f.idFactura = :id")
    Optional<Factura> findByIdParaActualizar(@Param("id") Long id);

    @Query(value = "select nextval('seq_numero_factura')", nativeQuery = true)
    Long siguienteSecuencia();
}
