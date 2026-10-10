package com.pasteleria.facturacion.presentation.dto;

import java.util.List;

public record ReporteMensualResponse(int mes, int anio, List<ReporteProductoResponse> productos) {
}
