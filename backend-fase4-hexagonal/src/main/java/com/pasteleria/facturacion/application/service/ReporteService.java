package com.pasteleria.facturacion.application.service;

import com.pasteleria.facturacion.application.port.in.ConsultarReporteUseCase;
import com.pasteleria.facturacion.application.port.out.ReporteRepositoryPort;
import com.pasteleria.facturacion.domain.model.PeriodoMensual;
import com.pasteleria.facturacion.domain.model.ReporteMensual;

import org.springframework.transaction.annotation.Transactional;

/** Aplicación: reporte mensual. La consolidación es una regla pura del dominio. */
@Transactional(readOnly = true)
public class ReporteService implements ConsultarReporteUseCase {

    private final ReporteRepositoryPort reportes;

    public ReporteService(ReporteRepositoryPort reportes) {
        this.reportes = reportes;
    }

    @Override
    public ReporteMensual ventasDelMes(int mes, int anio) {
        PeriodoMensual periodo = new PeriodoMensual(mes, anio);
        return ReporteMensual.consolidar(mes, anio, reportes.buscarLineasVendidas(periodo.desde(), periodo.hasta()));
    }
}
