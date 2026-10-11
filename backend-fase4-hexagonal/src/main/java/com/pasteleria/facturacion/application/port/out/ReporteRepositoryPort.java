package com.pasteleria.facturacion.application.port.out;

import com.pasteleria.facturacion.domain.model.LineaVentaReporte;

import java.time.LocalDateTime;
import java.util.List;

/** Puerto de salida: líneas vendidas en un rango de fechas [desde, hasta). */
public interface ReporteRepositoryPort {

    List<LineaVentaReporte> buscarLineasVendidas(LocalDateTime desde, LocalDateTime hasta);
}
