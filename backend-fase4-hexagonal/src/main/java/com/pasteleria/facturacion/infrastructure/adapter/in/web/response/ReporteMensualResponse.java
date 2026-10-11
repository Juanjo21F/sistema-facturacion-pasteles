package com.pasteleria.facturacion.infrastructure.adapter.in.web.response;

import java.util.List;

public record ReporteMensualResponse(int mes, int anio, List<ReporteProductoResponse> productos) {
}
