package com.pasteleria.facturacion.dataaccess.repository;

import com.pasteleria.facturacion.dataaccess.entity.DetalleFacturaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DetalleFacturaRepository extends JpaRepository<DetalleFacturaEntity, Long> {

    @Query("""
            select new com.pasteleria.facturacion.dataaccess.repository.FilaReporteVenta(
                p.idProducto, p.codigoUnico, p.nombre, d.cantidad, d.subtotal, f.estadoFactura)
            from DetalleFacturaEntity d
            join d.factura f
            join d.producto p
            where f.fechaEmision >= :desde and f.fechaEmision < :hasta
            """)
    List<FilaReporteVenta> findFilasEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
