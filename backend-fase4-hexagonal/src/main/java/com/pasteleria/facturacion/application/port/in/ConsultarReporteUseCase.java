package com.pasteleria.facturacion.application.port.in;

import com.pasteleria.facturacion.domain.model.ReporteMensual;

/** Puerto de entrada: reporte mensual de productos más vendidos. */
public interface ConsultarReporteUseCase {

    ReporteMensual ventasDelMes(int mes, int anio);
}
