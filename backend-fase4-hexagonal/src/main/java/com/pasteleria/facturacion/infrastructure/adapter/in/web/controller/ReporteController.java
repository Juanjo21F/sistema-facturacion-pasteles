package com.pasteleria.facturacion.infrastructure.adapter.in.web.controller;

import com.pasteleria.facturacion.domain.model.Rol;
import com.pasteleria.facturacion.application.port.in.ConsultarReporteUseCase;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.mapper.WebMapper;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.ReporteMensualResponse;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.security.RolesPermitidos;

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

    private final ConsultarReporteUseCase reportes;

    public ReporteController(ConsultarReporteUseCase reportes) {
        this.reportes = reportes;
    }

    @GetMapping("/ventas")
    public ReporteMensualResponse ventasDelMes(
            @RequestParam @Min(value = 1, message = "El mes debe estar entre 1 y 12")
            @Max(value = 12, message = "El mes debe estar entre 1 y 12") int mes,
            @RequestParam @Min(value = 2000, message = "El año debe ser mayor o igual a 2000") int anio) {
        return WebMapper.aRespuesta(reportes.ventasDelMes(mes, anio));
    }
}
