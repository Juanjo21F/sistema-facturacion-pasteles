package com.pasteleria.facturacion.repositorios;

import com.pasteleria.facturacion.modelo.entidades.DetalleFactura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DetalleFacturaRepositorio extends JpaRepository<DetalleFactura, Long> {

    @Query("""
            select new com.pasteleria.facturacion.repositorios.FilaReporteVenta(
                p.id, p.codigoUnico, p.nombre, d.cantidad, d.subtotal, f.estado)
            from DetalleFactura d
            join d.factura f
            join d.producto p
            where f.fechaEmision >= :desde and f.fechaEmision < :hasta
            """)
    List<FilaReporteVenta> findFilasEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
