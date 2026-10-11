package com.pasteleria.facturacion.domain;

import com.pasteleria.facturacion.domain.model.EstadoFactura;
import com.pasteleria.facturacion.domain.model.LineaVentaReporte;
import com.pasteleria.facturacion.domain.model.ReporteMensual;
import com.pasteleria.facturacion.domain.model.ReporteProductoVendido;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Regla pura del dominio: no necesita Spring, ni Mockito, ni base de datos. */
class ReporteMensualTest {

    private LineaVentaReporte linea(long id, String nombre, int cantidad, String subtotal, EstadoFactura estado) {
        return new LineaVentaReporte(id, "C" + id, nombre, cantidad, new BigDecimal(subtotal), estado);
    }

    @Test
    void excluyeFacturasAnuladas() { // Prueba 15
        ReporteMensual reporte = ReporteMensual.consolidar(10, 2026, List.of(
                linea(1, "Torta", 2, "90000", EstadoFactura.EMITIDA),
                linea(1, "Torta", 5, "225000", EstadoFactura.ANULADA),
                linea(2, "Cupcake", 4, "24000", EstadoFactura.ANULADA)));

        assertThat(reporte.productos()).hasSize(1);
        assertThat(reporte.productos().get(0).cantidadVendida()).isEqualTo(2);
        assertThat(reporte.productos().get(0).totalIngresos()).isEqualByComparingTo("90000");
    }

    @Test
    void agrupaPorProductoYOrdenaDeMayorAMenorCantidad() { // Prueba 16
        ReporteMensual reporte = ReporteMensual.consolidar(10, 2026, List.of(
                linea(1, "Torta", 2, "90000", EstadoFactura.EMITIDA),
                linea(2, "Cupcake", 10, "60000", EstadoFactura.EMITIDA),
                linea(3, "Galleta", 5, "10000", EstadoFactura.EMITIDA),
                linea(1, "Torta", 4, "180000", EstadoFactura.EMITIDA)));

        assertThat(reporte.productos()).extracting(ReporteProductoVendido::nombreProducto)
                .containsExactly("Cupcake", "Torta", "Galleta");
        assertThat(reporte.productos().get(1).cantidadVendida()).isEqualTo(6);
        assertThat(reporte.productos().get(1).totalIngresos()).isEqualByComparingTo("270000");
    }
}
