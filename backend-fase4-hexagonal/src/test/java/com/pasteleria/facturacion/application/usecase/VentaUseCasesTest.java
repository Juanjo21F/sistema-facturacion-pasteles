package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.application.service.AuditoriaService;
import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.FacturaYaAnuladaException;
import com.pasteleria.facturacion.domain.exception.StockInsuficienteException;
import com.pasteleria.facturacion.domain.port.input.command.ItemVentaCommand;
import com.pasteleria.facturacion.domain.port.input.command.RegistrarVentaCommand;
import com.pasteleria.facturacion.domain.service.FacturacionDomainService;
import com.pasteleria.facturacion.domain.service.InventarioDomainService;
import com.pasteleria.facturacion.domain.valueobject.EstadoFactura;
import com.pasteleria.facturacion.domain.valueobject.EstadoRegistro;
import com.pasteleria.facturacion.domain.valueobject.Rol;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import com.pasteleria.facturacion.support.InMemoryCategoriaRepository;
import com.pasteleria.facturacion.support.InMemoryClienteRepository;
import com.pasteleria.facturacion.support.InMemoryFacturaRepository;
import com.pasteleria.facturacion.support.InMemoryProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VentaUseCasesTest {

    private final InMemoryClienteRepository clientes = new InMemoryClienteRepository();
    private final InMemoryProductoRepository productos = new InMemoryProductoRepository();
    private final InMemoryFacturaRepository facturas = new InMemoryFacturaRepository();
    private final UsuarioAutenticado usuario = new UsuarioAutenticado("u-1", "ana", Rol.ADMINISTRADOR);

    private RegistrarVentaUseCaseImpl registrarVenta;
    private AnularFacturaUseCaseImpl anularFactura;
    private Cliente cliente;
    private Producto torta;

    @BeforeEach
    void preparar() {
        FacturacionDomainService facturacion = new FacturacionDomainService(new InventarioDomainService());
        AuditoriaService auditoria = new AuditoriaService();
        Clock reloj = Clock.fixed(Instant.parse("2026-10-05T15:00:00Z"), ZoneOffset.UTC);
        registrarVenta = new RegistrarVentaUseCaseImpl(clientes, productos, facturas, facturacion, auditoria, reloj);
        anularFactura = new AnularFacturaUseCaseImpl(facturas, productos, facturacion, auditoria);

        cliente = clientes.guardar(Cliente.crear("123", "Ana Pérez", "300", "ana@mail.com"));
        torta = productos.guardar(Producto.crear("TOR-1", "Torta", InMemoryCategoriaRepository.TORTAS,
                new BigDecimal("45000"), 10));
    }

    private RegistrarVentaCommand venta(int cantidad) {
        return new RegistrarVentaCommand(cliente.getIdCliente(),
                List.of(new ItemVentaCommand(torta.getIdProducto(), cantidad)));
    }

    @Test
    void registraVentaGuardaFacturaYDescuentaStock() { // Prueba 7
        Factura f = registrarVenta.ejecutar(venta(3), usuario);

        assertThat(f.getIdFactura()).isNotNull();
        assertThat(f.getTotal()).isEqualByComparingTo("135000");
        assertThat(f.getUsuarioAuthcore()).isEqualTo("u-1");
        assertThat(facturas.cantidadGuardada()).isEqualTo(1);
        assertThat(productos.buscarPorId(torta.getIdProducto()).orElseThrow().getStock()).isEqualTo(7);
    }

    @Test
    void ventaConStockInsuficienteNoDejaFacturaNiDescuentos() { // Prueba 10 (caso de uso)
        assertThatThrownBy(() -> registrarVenta.ejecutar(venta(11), usuario))
                .isInstanceOf(StockInsuficienteException.class);

        assertThat(facturas.cantidadGuardada()).isZero();
        assertThat(productos.buscarPorId(torta.getIdProducto()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void anularFacturaRestauraStockUnaSolaVez() { // Pruebas 12, 13 y 14 (caso de uso)
        Factura f = registrarVenta.ejecutar(venta(3), usuario);

        Factura anulada = anularFactura.ejecutar(f.getIdFactura(), usuario);

        assertThat(anulada.getEstadoFactura()).isEqualTo(EstadoFactura.ANULADA);
        assertThat(productos.buscarPorId(torta.getIdProducto()).orElseThrow().getStock()).isEqualTo(10);

        assertThatThrownBy(() -> anularFactura.ejecutar(f.getIdFactura(), usuario))
                .isInstanceOf(FacturaYaAnuladaException.class);
        assertThat(productos.buscarPorId(torta.getIdProducto()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void noPermiteVenderAClienteInactivo() {
        cliente.desactivar();
        clientes.guardar(cliente);

        assertThatThrownBy(() -> registrarVenta.ejecutar(venta(1), usuario))
                .hasMessageContaining("inactivo");
        assertThat(cliente.getEstado()).isEqualTo(EstadoRegistro.INACTIVO);
        assertThat(facturas.cantidadGuardada()).isZero();
    }
}
