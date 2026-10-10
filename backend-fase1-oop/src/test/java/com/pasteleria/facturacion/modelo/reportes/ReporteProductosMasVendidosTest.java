package com.pasteleria.facturacion.modelo.reportes;

import com.pasteleria.facturacion.modelo.enumeraciones.EstadoFactura;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReporteProductosMasVendidosTest {

    /** Se usa a través de la clase abstracta: el algoritmo (generar) es de ReporteDeVentas. */
    private final ReporteDeVentas<ReporteProductoVendido> reporte = new ReporteProductosMasVendidos();

    private LineaVentaReporte linea(long id, String nombre, int cantidad, String subtotal, EstadoFactura estado) {
        return new LineaVentaReporte(id, "C" + id, nombre, cantidad, new BigDecimal(subtotal), estado);
    }

    @Test
    void excluyeFacturasAnuladas() { // Prueba 15
        List<ReporteProductoVendido> r = reporte.generar(List.of(
                linea(1, "Torta", 2, "90000", EstadoFactura.EMITIDA),
                linea(1, "Torta", 5, "225000", EstadoFactura.ANULADA),
                linea(2, "Cupcake", 4, "24000", EstadoFactura.ANULADA)));

        assertThat(r).hasSize(1);
        assertThat(r.get(0).cantidadVendida()).isEqualTo(2);
        assertThat(r.get(0).totalIngresos()).isEqualByComparingTo("90000");
    }

    @Test
    void agrupaPorProductoYOrdenaDeMayorAMenorCantidad() { // Prueba 16
        List<ReporteProductoVendido> r = reporte.generar(List.of(
                linea(1, "Torta", 2, "90000", EstadoFactura.EMITIDA),
                linea(2, "Cupcake", 10, "60000", EstadoFactura.EMITIDA),
                linea(3, "Galleta", 5, "10000", EstadoFactura.EMITIDA),
                linea(1, "Torta", 4, "180000", EstadoFactura.EMITIDA)));

        assertThat(r).extracting(ReporteProductoVendido::nombreProducto).containsExactly("Cupcake", "Torta", "Galleta");
        assertThat(r.get(1).cantidadVendida()).isEqualTo(6);
        assertThat(r.get(1).totalIngresos()).isEqualByComparingTo("270000");
    }
}
