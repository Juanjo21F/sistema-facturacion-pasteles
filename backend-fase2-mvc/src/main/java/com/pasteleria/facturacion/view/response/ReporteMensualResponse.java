package com.pasteleria.facturacion.view.response;

import java.util.List;

public record ReporteMensualResponse(int mes, int anio, List<ReporteProductoResponse> productos) {
}
