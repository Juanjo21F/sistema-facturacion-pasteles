package com.pasteleria.facturacion.application.service;

import com.pasteleria.facturacion.application.port.in.ItemVentaCommand;
import com.pasteleria.facturacion.application.port.in.RegistrarVentaCommand;
import com.pasteleria.facturacion.application.port.out.AuditoriaPort;
import com.pasteleria.facturacion.application.port.out.ClienteRepositoryPort;
import com.pasteleria.facturacion.application.port.out.FacturaRepositoryPort;
import com.pasteleria.facturacion.application.port.out.ProductoRepositoryPort;
import com.pasteleria.facturacion.domain.exception.ClienteInactivoException;
import com.pasteleria.facturacion.domain.exception.FacturaYaAnuladaException;
import com.pasteleria.facturacion.domain.exception.ProductoInactivoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.exception.StockInsuficienteException;
import com.pasteleria.facturacion.domain.model.Categoria;
import com.pasteleria.facturacion.domain.model.Cliente;
import com.pasteleria.facturacion.domain.model.DetalleFactura;
import com.pasteleria.facturacion.domain.model.EstadoFactura;
import com.pasteleria.facturacion.domain.model.EstadoRegistro;
import com.pasteleria.facturacion.domain.model.Factura;
import com.pasteleria.facturacion.domain.model.Producto;
import com.pasteleria.facturacion.domain.model.Rol;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de venta y anulación con los puertos de salida simulados. */
class FacturacionServiceTest {

    private final ClienteRepositoryPort clientes = mock(ClienteRepositoryPort.class);
    private final ProductoRepositoryPort productos = mock(ProductoRepositoryPort.class);
    private final FacturaRepositoryPort facturas = mock(FacturaRepositoryPort.class);
    private final Clock reloj = Clock.fixed(Instant.parse("2026-10-05T15:00:00Z"), ZoneOffset.UTC);
    private final UsuarioAutenticado usuario = new UsuarioAutenticado("u-1", "ana", Rol.ADMINISTRADOR);
    private final FacturacionService servicio =
            new FacturacionService(clientes, productos, facturas, mock(AuditoriaPort.class), reloj);

    private Cliente cliente;
    private Producto torta;     // 45.000 x stock 10
    private Producto cupcake;   // 6.000 x stock 20

    private Producto producto(long id, String nombre, String precio, int stock) {
        return new Producto(id, "P-" + id, nombre, new Categoria(1L, "Tortas"), new BigDecimal(precio), stock,
                EstadoRegistro.ACTIVO);
    }

    @BeforeEach
    void preparar() {
        cliente = new Cliente(1L, "123", "Ana Pérez", "300", "a@a.com", EstadoRegistro.ACTIVO);
        torta = producto(1, "Torta", "45000", 10);
        cupcake = producto(2, "Cupcake", "6000", 20);
        when(clientes.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        when(productos.buscarPorIdsParaActualizar(any())).thenReturn(List.of(torta, cupcake));
        when(facturas.siguienteSecuencia()).thenReturn(1L);
        when(facturas.guardar(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private RegistrarVentaCommand venta(ItemVentaCommand... items) {
        return new RegistrarVentaCommand(1L, List.of(items));
    }

    @Test
    void calculaSubtotalYTotalYDescuentaStock() { // Pruebas 7, 8, 9 y 11
        Factura f = servicio.registrarVenta(venta(new ItemVentaCommand(1L, 2), new ItemVentaCommand(2L, 3)), usuario);

        assertThat(f.getDetalles().get(0).subtotal()).isEqualByComparingTo("90000");
        assertThat(f.getTotal()).isEqualByComparingTo("108000");
        assertThat(f.getNumeroFactura()).isEqualTo("FAC-2026-000001");
        assertThat(f.getEstado()).isEqualTo(EstadoFactura.EMITIDA);
        assertThat(f.getUsuarioAuthcore()).isEqualTo("u-1");
        assertThat(torta.getStock()).isEqualTo(8);
        assertThat(cupcake.getStock()).isEqualTo(17);
    }

    @Test
    void ventaConStockInsuficienteNoDejaFacturaNiDescuentos() { // Prueba 10
        assertThatThrownBy(() -> servicio.registrarVenta(
                venta(new ItemVentaCommand(2L, 5), new ItemVentaCommand(1L, 11)), usuario))
                .isInstanceOf(StockInsuficienteException.class);

        assertThat(torta.getStock()).isEqualTo(10);
        assertThat(cupcake.getStock()).isEqualTo(20);
        verify(facturas, never()).guardar(any());
    }

    @Test
    void noPermiteVenderProductoInactivoClienteInactivoNiProductoRepetido() {
        Producto inactivo = new Producto(1L, "P-1", "Torta", new Categoria(1L, "Tortas"), new BigDecimal("45000"), 10,
                EstadoRegistro.INACTIVO);
        when(productos.buscarPorIdsParaActualizar(any())).thenReturn(List.of(inactivo));
        assertThatThrownBy(() -> servicio.registrarVenta(venta(new ItemVentaCommand(1L, 1)), usuario))
                .isInstanceOf(ProductoInactivoException.class);

        when(productos.buscarPorIdsParaActualizar(any())).thenReturn(List.of(torta, cupcake));
        assertThatThrownBy(() -> servicio.registrarVenta(
                venta(new ItemVentaCommand(1L, 1), new ItemVentaCommand(1L, 2)), usuario))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("repetido");

        cliente.desactivar();
        assertThatThrownBy(() -> servicio.registrarVenta(venta(new ItemVentaCommand(2L, 1)), usuario))
                .isInstanceOf(ClienteInactivoException.class);
        assertThat(cupcake.getStock()).isEqualTo(20);
    }

    @Test
    void anulaFacturaRestauraStockUnaSolaVez() { // Pruebas 12, 13 y 14
        torta = producto(1, "Torta", "45000", 6);
        Factura factura = new Factura(5L, "FAC-2026-000005", LocalDateTime.now(), cliente, "u-1", BigDecimal.ONE,
                EstadoFactura.EMITIDA, List.of(new DetalleFactura(1L, torta, 4, new BigDecimal("45000"), new BigDecimal("180000"))));
        when(facturas.buscarPorIdParaActualizar(5L)).thenReturn(Optional.of(factura));
        when(facturas.buscarPorId(5L)).thenReturn(Optional.of(factura));
        when(productos.buscarPorIdsParaActualizar(any())).thenReturn(List.of(torta));

        servicio.anular(5L, usuario);

        assertThat(factura.getEstado()).isEqualTo(EstadoFactura.ANULADA);
        assertThat(torta.getStock()).isEqualTo(10);

        assertThatThrownBy(() -> servicio.anular(5L, usuario)).isInstanceOf(FacturaYaAnuladaException.class);
        assertThat(torta.getStock()).isEqualTo(10); // no pasó de 10 a 14
    }
}
