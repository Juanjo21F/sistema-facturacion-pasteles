package com.pasteleria.facturacion.model.entity;

import com.pasteleria.facturacion.exception.ClienteInactivoException;
import com.pasteleria.facturacion.exception.FacturaYaAnuladaException;
import com.pasteleria.facturacion.exception.ProductoInactivoException;
import com.pasteleria.facturacion.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.exception.StockInsuficienteException;
import com.pasteleria.facturacion.model.enums.EstadoFactura;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacturaTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 5, 10, 0);
    private final Categoria categoria = new Categoria(1L, "Tortas", "Tortas grandes");

    private Cliente cliente;
    private Producto torta;     // 45.000 x stock 10
    private Producto cupcake;   // 6.000 x stock 20

    @BeforeEach
    void preparar() {
        cliente = Cliente.crear("123", "Ana Pérez", "300", "ana@mail.com");
        torta = Producto.crear("TOR-1", "Torta", categoria, new BigDecimal("45000"), 10);
        cupcake = Producto.crear("CUP-1", "Cupcake", categoria, new BigDecimal("6000"), 20);
        ReflectionTestUtils.setField(torta, "idProducto", 1L);
        ReflectionTestUtils.setField(cupcake, "idProducto", 2L);
    }

    private Factura emitir(LineaVenta... lineas) {
        return Factura.emitir(cliente, List.of(lineas), "u-1", 1, FECHA);
    }

    @Test
    void calculaSubtotalCorrectamente() { // Prueba 8
        Factura f = emitir(new LineaVenta(torta, 2));

        assertThat(f.getDetalles().get(0).getSubtotal()).isEqualByComparingTo("90000");
    }

    @Test
    void calculaTotalCorrectamente() { // Prueba 9
        Factura f = emitir(new LineaVenta(torta, 2), new LineaVenta(cupcake, 3));

        assertThat(f.getTotal()).isEqualByComparingTo("108000"); // 90.000 + 18.000
        assertThat(f.getNumeroFactura()).isEqualTo("FAC-2026-000001");
        assertThat(f.getEstadoFactura()).isEqualTo(EstadoFactura.EMITIDA);
    }

    @Test
    void rechazaVentaPorStockInsuficienteYNoDescuentaNada() { // Prueba 10
        assertThatThrownBy(() -> emitir(new LineaVenta(cupcake, 5), new LineaVenta(torta, 11)))
                .isInstanceOf(StockInsuficienteException.class);

        assertThat(torta.getStock()).isEqualTo(10);
        assertThat(cupcake.getStock()).isEqualTo(20); // tampoco se descontó la línea válida
    }

    @Test
    void descuentaStockCorrectamente() { // Prueba 11
        emitir(new LineaVenta(torta, 4), new LineaVenta(cupcake, 20));

        assertThat(torta.getStock()).isEqualTo(6);
        assertThat(cupcake.getStock()).isZero();
    }

    @Test
    void noPermiteVenderProductoInactivoNiClienteInactivo() {
        torta.desactivar();
        assertThatThrownBy(() -> emitir(new LineaVenta(torta, 1))).isInstanceOf(ProductoInactivoException.class);

        cliente.desactivar();
        assertThatThrownBy(() -> emitir(new LineaVenta(cupcake, 1))).isInstanceOf(ClienteInactivoException.class);
        assertThat(cupcake.getStock()).isEqualTo(20);
    }

    @Test
    void noPermiteRepetirProductoEnLaMismaFactura() {
        assertThatThrownBy(() -> emitir(new LineaVenta(torta, 1), new LineaVenta(torta, 2)))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("repetido");
    }

    @Test
    void anulaFacturaYRestauraStock() { // Pruebas 12 y 13
        Factura f = emitir(new LineaVenta(torta, 4), new LineaVenta(cupcake, 5));

        f.anular(Map.of(1L, torta, 2L, cupcake));

        assertThat(f.getEstadoFactura()).isEqualTo(EstadoFactura.ANULADA);
        assertThat(torta.getStock()).isEqualTo(10);
        assertThat(cupcake.getStock()).isEqualTo(20);
    }

    @Test
    void evitaDobleAnulacionYNoRestauraDosVeces() { // Prueba 14
        Factura f = emitir(new LineaVenta(torta, 4));
        Map<Long, Producto> productos = Map.of(1L, torta);
        f.anular(productos);

        assertThatThrownBy(() -> f.anular(productos)).isInstanceOf(FacturaYaAnuladaException.class);
        assertThat(torta.getStock()).isEqualTo(10); // no pasó de 10 a 14
    }
}
