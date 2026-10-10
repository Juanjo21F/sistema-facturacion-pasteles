package com.pasteleria.facturacion.domain.service;

import com.pasteleria.facturacion.domain.valueobject.EstadoFactura;
import com.pasteleria.facturacion.domain.valueobject.LineaVentaReporte;
import com.pasteleria.facturacion.domain.valueobject.ReporteProductoVendido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReporteVentasDomainServiceTest {

    private final ReporteVentasDomainService servicio = new ReporteVentasDomainService();

    private LineaVentaReporte linea(long id, String nombre, int cantidad, String subtotal, EstadoFactura estado) {
        return new LineaVentaReporte(id, "C" + id, nombre, cantidad, new BigDecimal(subtotal), estado);
    }

    @Test
    void excluyeFacturasAnuladas() { // Prueba 15
        List<ReporteProductoVendido> reporte = servicio.generar(List.of(
                linea(1, "Torta", 2, "90000", EstadoFactura.EMITIDA),
                linea(1, "Torta", 5, "225000", EstadoFactura.ANULADA),
                linea(2, "Cupcake", 4, "24000", EstadoFactura.ANULADA)));

        assertThat(reporte).hasSize(1);
        assertThat(reporte.get(0).cantidadVendida()).isEqualTo(2);
        assertThat(reporte.get(0).totalIngresos()).isEqualByComparingTo("90000");
    }

    @Test
    void agrupaPorProductoYOrdenaDeMayorAMenorCantidad() { // Prueba 16
        List<ReporteProductoVendido> reporte = servicio.generar(List.of(
                linea(1, "Torta", 2, "90000", EstadoFactura.EMITIDA),
                linea(2, "Cupcake", 10, "60000", EstadoFactura.EMITIDA),
                linea(3, "Galleta", 5, "10000", EstadoFactura.EMITIDA),
                linea(1, "Torta", 4, "180000", EstadoFactura.EMITIDA)));

        assertThat(reporte).extracting(ReporteProductoVendido::nombreProducto)
                .containsExactly("Cupcake", "Torta", "Galleta");
        assertThat(reporte.get(1).cantidadVendida()).isEqualTo(6);
        assertThat(reporte.get(1).totalIngresos()).isEqualByComparingTo("270000");
    }
}
