package com.pasteleria.facturacion.domain.service;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.FacturaYaAnuladaException;
import com.pasteleria.facturacion.domain.exception.ProductoInactivoException;
import com.pasteleria.facturacion.domain.exception.StockInsuficienteException;
import com.pasteleria.facturacion.domain.valueobject.EstadoFactura;
import com.pasteleria.facturacion.domain.valueobject.LineaVenta;
import com.pasteleria.facturacion.domain.valueobject.Rol;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import com.pasteleria.facturacion.support.InMemoryCategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacturacionDomainServiceTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 5, 10, 0);
    private final FacturacionDomainService servicio = new FacturacionDomainService(new InventarioDomainService());
    private final UsuarioAutenticado usuario = new UsuarioAutenticado("u-1", "ana", Rol.EMPLEADO);

    private Cliente cliente;
    private Producto torta;     // 45.000 x stock 10
    private Producto cupcake;   // 6.000 x stock 20

    @BeforeEach
    void preparar() {
        cliente = Cliente.reconstruir(1L, "123", "Ana Pérez", "300", "ana@mail.com",
                com.pasteleria.facturacion.domain.valueobject.EstadoRegistro.ACTIVO);
        torta = Producto.reconstruir(1L, "TOR-1", "Torta", InMemoryCategoriaRepository.TORTAS,
                new BigDecimal("45000"), 10, com.pasteleria.facturacion.domain.valueobject.EstadoRegistro.ACTIVO);
        cupcake = Producto.reconstruir(2L, "CUP-1", "Cupcake", InMemoryCategoriaRepository.TORTAS,
                new BigDecimal("6000"), 20, com.pasteleria.facturacion.domain.valueobject.EstadoRegistro.ACTIVO);
    }

    private Factura emitir(LineaVenta... lineas) {
        return servicio.emitirFactura(cliente, List.of(lineas), usuario, 1, FECHA);
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
    void noPermiteVenderProductoInactivo() {
        torta.desactivar();

        assertThatThrownBy(() -> emitir(new LineaVenta(torta, 1))).isInstanceOf(ProductoInactivoException.class);
    }

    @Test
    void anulaFacturaYRestauraStock() { // Pruebas 12 y 13
        Factura f = emitir(new LineaVenta(torta, 4), new LineaVenta(cupcake, 5));

        servicio.anularFactura(f, Map.of(1L, torta, 2L, cupcake));

        assertThat(f.getEstadoFactura()).isEqualTo(EstadoFactura.ANULADA);
        assertThat(torta.getStock()).isEqualTo(10);
        assertThat(cupcake.getStock()).isEqualTo(20);
    }

    @Test
    void evitaDobleAnulacionYNoRestauraDosVeces() { // Prueba 14
        Factura f = emitir(new LineaVenta(torta, 4));
        Map<Long, Producto> productos = Map.of(1L, torta);
        servicio.anularFactura(f, productos);

        assertThatThrownBy(() -> servicio.anularFactura(f, productos)).isInstanceOf(FacturaYaAnuladaException.class);
        assertThat(torta.getStock()).isEqualTo(10); // no pasó de 10 a 14
    }
}
