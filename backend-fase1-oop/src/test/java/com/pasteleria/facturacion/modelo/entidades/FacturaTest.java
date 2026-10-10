package com.pasteleria.facturacion.modelo.entidades;

import com.pasteleria.facturacion.modelo.enumeraciones.EstadoFactura;
import com.pasteleria.facturacion.modelo.excepciones.ClienteInactivoException;
import com.pasteleria.facturacion.modelo.excepciones.FacturaYaAnuladaException;
import com.pasteleria.facturacion.modelo.excepciones.ProductoInactivoException;
import com.pasteleria.facturacion.modelo.excepciones.ReglaDeNegocioException;
import com.pasteleria.facturacion.modelo.excepciones.StockInsuficienteException;
import com.pasteleria.facturacion.modelo.reglas.ContextoVenta;
import com.pasteleria.facturacion.modelo.reglas.ReglaClienteActivo;
import com.pasteleria.facturacion.modelo.reglas.ReglaDeVenta;
import com.pasteleria.facturacion.modelo.reglas.ReglaProductosDisponibles;
import com.pasteleria.facturacion.modelo.reglas.ReglaSinProductosRepetidos;
import com.pasteleria.facturacion.modelo.reglas.ReglaStockSuficiente;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Emisión y anulación: las reglas se aplican recorriendo la lista polimórfica de ReglaDeVenta. */
class FacturaTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 5, 10, 0);
    private final Categoria categoria = new Categoria("Tortas", "Tortas grandes");
    private final List<ReglaDeVenta> reglas = List.of(new ReglaProductosDisponibles(),
            new ReglaSinProductosRepetidos(), new ReglaClienteActivo(), new ReglaStockSuficiente());

    private Cliente cliente;
    private Producto torta;     // 45.000 x stock 10
    private Producto cupcake;   // 6.000 x stock 20

    @BeforeEach
    void preparar() {
        cliente = Cliente.crear("123", "Ana Pérez", "300", "ana@mail.com");
        torta = Producto.crear("TOR-1", "Torta", categoria, new BigDecimal("45000"), 10);
        cupcake = Producto.crear("CUP-1", "Cupcake", categoria, new BigDecimal("6000"), 20);
        ReflectionTestUtils.setField(torta, "id", 1L);
        ReflectionTestUtils.setField(cupcake, "id", 2L);
    }

    /** Lo mismo que hace ServicioFacturacion: validar con todas las reglas, emitir y descontar. */
    private Factura emitir(LineaVenta... lineas) {
        List<LineaVenta> lista = List.of(lineas);
        reglas.forEach(r -> r.validar(new ContextoVenta(cliente, lista)));
        Factura factura = Factura.emitir(Factura.generarNumero(1, FECHA), FECHA, cliente, "u-1", lista);
        lista.forEach(l -> l.producto().descontarStock(l.cantidad()));
        return factura;
    }

    private void anular(Factura f) {
        f.anular();
        f.getDetalles().forEach(d -> d.getProducto().restaurarStock(d.getCantidad()));
    }

    @Test
    void calculaSubtotalCorrectamente() { // Prueba 8
        assertThat(emitir(new LineaVenta(torta, 2)).getDetalles().get(0).getSubtotal()).isEqualByComparingTo("90000");
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
        assertThat(cupcake.getStock()).isEqualTo(20);
    }

    @Test
    void descuentaStockCorrectamente() { // Prueba 11
        emitir(new LineaVenta(torta, 4), new LineaVenta(cupcake, 20));

        assertThat(torta.getStock()).isEqualTo(6);
        assertThat(cupcake.getStock()).isZero();
    }

    @Test
    void cadaReglaRechazaSuCaso() {
        assertThatThrownBy(() -> emitir(new LineaVenta(torta, 1), new LineaVenta(torta, 2)))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("repetido");

        torta.desactivar();
        assertThatThrownBy(() -> emitir(new LineaVenta(torta, 1))).isInstanceOf(ProductoInactivoException.class);

        cliente.desactivar();
        assertThatThrownBy(() -> emitir(new LineaVenta(cupcake, 1))).isInstanceOf(ClienteInactivoException.class);
        assertThat(cupcake.getStock()).isEqualTo(20);
    }

    @Test
    void anulaFacturaYRestauraStock() { // Pruebas 12 y 13
        Factura f = emitir(new LineaVenta(torta, 4), new LineaVenta(cupcake, 5));

        anular(f);

        assertThat(f.estaAnulada()).isTrue();
        assertThat(torta.getStock()).isEqualTo(10);
        assertThat(cupcake.getStock()).isEqualTo(20);
    }

    @Test
    void evitaDobleAnulacionYNoRestauraDosVeces() { // Prueba 14
        Factura f = emitir(new LineaVenta(torta, 4));
        anular(f);

        assertThatThrownBy(() -> anular(f)).isInstanceOf(FacturaYaAnuladaException.class);
        assertThat(torta.getStock()).isEqualTo(10);
    }
}
