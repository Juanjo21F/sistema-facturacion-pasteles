package com.pasteleria.facturacion.presentation.controller;

import com.pasteleria.facturacion.domain.port.input.GenerarReporteMensualUseCase;
import com.pasteleria.facturacion.domain.valueobject.Rol;
import com.pasteleria.facturacion.presentation.dto.ApiMapper;
import com.pasteleria.facturacion.presentation.dto.ReporteMensualResponse;
import com.pasteleria.facturacion.presentation.security.RolesPermitidos;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@Validated
@RolesPermitidos(Rol.ADMINISTRADOR)
public class ReporteController {

    private final GenerarReporteMensualUseCase generarReporte;

    public ReporteController(GenerarReporteMensualUseCase generarReporte) {
        this.generarReporte = generarReporte;
    }

    @GetMapping("/ventas")
    public ReporteMensualResponse ventasDelMes(
            @RequestParam @Min(value = 1, message = "El mes debe estar entre 1 y 12")
            @Max(value = 12, message = "El mes debe estar entre 1 y 12") int mes,
            @RequestParam @Min(value = 2000, message = "El año debe ser mayor o igual a 2000") int anio) {
        return ApiMapper.aRespuesta(mes, anio, generarReporte.ejecutar(mes, anio));
    }
}
