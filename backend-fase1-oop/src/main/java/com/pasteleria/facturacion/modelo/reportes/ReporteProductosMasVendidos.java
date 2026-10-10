package com.pasteleria.facturacion.modelo.reportes;

import com.pasteleria.facturacion.modelo.enumeraciones.EstadoFactura;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/** Reporte concreto: excluye ANULADAS, suma por producto y ordena por cantidad descendente. */
public class ReporteProductosMasVendidos extends ReporteDeVentas<ReporteProductoVendido> {

    @Override
    protected boolean incluir(LineaVentaReporte linea) {
        return linea.estadoFactura() != EstadoFactura.ANULADA;
    }

    @Override
    protected ReporteProductoVendido consolidar(List<LineaVentaReporte> lineasDelProducto) {
        LineaVentaReporte primera = lineasDelProducto.get(0);
        long cantidad = lineasDelProducto.stream().mapToLong(LineaVentaReporte::cantidad).sum();
        BigDecimal ingresos = lineasDelProducto.stream()
                .map(LineaVentaReporte::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReporteProductoVendido(primera.productoId(), primera.codigoProducto(),
                primera.nombreProducto(), cantidad, ingresos);
    }

    @Override
    protected Comparator<ReporteProductoVendido> orden() {
        return Comparator.comparingLong(ReporteProductoVendido::cantidadVendida).reversed()
                .thenComparing(ReporteProductoVendido::nombreProducto);
    }
}
