package com.pasteleria.facturacion.api.controladores;

import com.pasteleria.facturacion.api.dto.ConversorDto;
import com.pasteleria.facturacion.api.dto.respuestas.ReporteMensualResponse;
import com.pasteleria.facturacion.modelo.enumeraciones.Rol;
import com.pasteleria.facturacion.seguridad.RolesPermitidos;
import com.pasteleria.facturacion.servicios.ServicioReportes;

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
public class ReporteControlador {

    private final ServicioReportes reportes;

    public ReporteControlador(ServicioReportes reportes) {
        this.reportes = reportes;
    }

    @GetMapping("/ventas")
    public ReporteMensualResponse ventasDelMes(
            @RequestParam @Min(value = 1, message = "El mes debe estar entre 1 y 12")
            @Max(value = 12, message = "El mes debe estar entre 1 y 12") int mes,
            @RequestParam @Min(value = 2000, message = "El año debe ser mayor o igual a 2000") int anio) {
        return ConversorDto.aRespuesta(mes, anio, reportes.ventasDelMes(mes, anio));
    }
}
