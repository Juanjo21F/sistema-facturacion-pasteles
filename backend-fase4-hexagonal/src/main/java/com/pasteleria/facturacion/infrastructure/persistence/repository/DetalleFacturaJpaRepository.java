package com.pasteleria.facturacion.infrastructure.persistence.repository;

import com.pasteleria.facturacion.infrastructure.persistence.entity.DetalleFacturaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DetalleFacturaJpaRepository extends JpaRepository<DetalleFacturaJpaEntity, Long> {

    @Query("""
            select new com.pasteleria.facturacion.infrastructure.persistence.repository.FilaReporteVenta(
                p.id, p.codigoUnico, p.nombre, d.cantidad, d.subtotal, f.estado)
            from DetalleFacturaJpaEntity d
            join d.factura f
            join d.producto p
            where f.fechaEmision >= :desde and f.fechaEmision < :hasta
            """)
    List<FilaReporteVenta> findFilasEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
