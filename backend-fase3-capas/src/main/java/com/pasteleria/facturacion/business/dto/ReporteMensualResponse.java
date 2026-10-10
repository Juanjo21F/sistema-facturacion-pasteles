package com.pasteleria.facturacion.business.dto;

import java.util.List;

public record ReporteMensualResponse(int mes, int anio, List<ReporteProductoResponse> productos) {
}
