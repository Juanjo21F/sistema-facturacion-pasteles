package com.pasteleria.facturacion.domain.port.output;

import com.pasteleria.facturacion.domain.valueobject.LineaVentaReporte;
import com.pasteleria.facturacion.domain.valueobject.PeriodoMensual;

import java.util.List;

public interface ReporteVentasPort {

    /** Líneas vendidas en el período, con el estado de su factura. Una sola consulta, sin N+1. */
    List<LineaVentaReporte> obtenerLineasDelPeriodo(PeriodoMensual periodo);
}
