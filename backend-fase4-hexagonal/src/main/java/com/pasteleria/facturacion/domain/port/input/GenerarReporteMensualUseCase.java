package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.valueobject.ReporteProductoVendido;

import java.util.List;

public interface GenerarReporteMensualUseCase {

    List<ReporteProductoVendido> ejecutar(int mes, int anio);
}
