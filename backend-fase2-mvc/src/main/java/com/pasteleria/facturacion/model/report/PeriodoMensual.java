package com.pasteleria.facturacion.model.report;

import com.pasteleria.facturacion.exception.ReglaDeNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodoMensual(int mes, int anio) {

    public PeriodoMensual {
        if (mes < 1 || mes > 12) {
            throw new ReglaDeNegocioException("El mes debe estar entre 1 y 12");
        }
        if (anio < 2000) {
            throw new ReglaDeNegocioException("El año debe ser mayor o igual a 2000");
        }
    }

    public LocalDateTime desde() {
        return LocalDate.of(anio, mes, 1).atStartOfDay();
    }

    /** Límite superior exclusivo: primer instante del mes siguiente. */
    public LocalDateTime hasta() {
        return desde().plusMonths(1);
    }
}
