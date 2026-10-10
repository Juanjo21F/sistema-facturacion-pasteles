package com.pasteleria.facturacion.api.dto.respuestas;

import java.util.List;

public record ReporteMensualResponse(int mes, int anio, List<ReporteProductoResponse> productos) {
}
