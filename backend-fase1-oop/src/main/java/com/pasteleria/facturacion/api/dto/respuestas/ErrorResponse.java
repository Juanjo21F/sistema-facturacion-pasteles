package com.pasteleria.facturacion.api.dto.respuestas;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(LocalDateTime timestamp, int status, String error, String mensaje, List<String> detalles) {

    public static ErrorResponse de(int status, String error, String mensaje, List<String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, detalles);
    }
}
