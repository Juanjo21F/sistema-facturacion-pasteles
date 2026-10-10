package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.domain.port.input.GenerarReporteMensualUseCase;
import com.pasteleria.facturacion.domain.port.output.ReporteVentasPort;
import com.pasteleria.facturacion.domain.service.ReporteVentasDomainService;
import com.pasteleria.facturacion.domain.valueobject.PeriodoMensual;
import com.pasteleria.facturacion.domain.valueobject.ReporteProductoVendido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GenerarReporteMensualUseCaseImpl implements GenerarReporteMensualUseCase {

    private final ReporteVentasPort reporteVentas;
    private final ReporteVentasDomainService reporteDomain;

    public GenerarReporteMensualUseCaseImpl(ReporteVentasPort reporteVentas, ReporteVentasDomainService reporteDomain) {
        this.reporteVentas = reporteVentas;
        this.reporteDomain = reporteDomain;
    }

    @Override
    public List<ReporteProductoVendido> ejecutar(int mes, int anio) {
        PeriodoMensual periodo = new PeriodoMensual(mes, anio);
        return reporteDomain.generar(reporteVentas.obtenerLineasDelPeriodo(periodo));
    }
}
