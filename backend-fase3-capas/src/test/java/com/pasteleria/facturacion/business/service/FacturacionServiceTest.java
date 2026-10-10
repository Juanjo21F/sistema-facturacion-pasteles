package com.pasteleria.facturacion.business.service;

import com.pasteleria.facturacion.business.dto.FacturaResponse;
import com.pasteleria.facturacion.business.dto.ItemVentaCommand;
import com.pasteleria.facturacion.business.dto.RegistrarVentaCommand;
import com.pasteleria.facturacion.business.exception.ClienteInactivoException;
import com.pasteleria.facturacion.business.exception.FacturaYaAnuladaException;
import com.pasteleria.facturacion.business.exception.ProductoInactivoException;
import com.pasteleria.facturacion.business.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.business.exception.StockInsuficienteException;
import com.pasteleria.facturacion.business.security.Rol;
import com.pasteleria.facturacion.business.security.UsuarioAutenticado;
import com.pasteleria.facturacion.dataaccess.entity.ClienteEntity;
import com.pasteleria.facturacion.dataaccess.entity.DetalleFacturaEntity;
import com.pasteleria.facturacion.dataaccess.entity.EstadoFactura;
import com.pasteleria.facturacion.dataaccess.entity.EstadoRegistro;
import com.pasteleria.facturacion.dataaccess.entity.FacturaEntity;
import com.pasteleria.facturacion.dataaccess.entity.ProductoEntity;
import com.pasteleria.facturacion.dataaccess.repository.ClienteRepository;
import com.pasteleria.facturacion.dataaccess.repository.FacturaRepository;
import com.pasteleria.facturacion.dataaccess.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
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

/** Reglas de venta y anulación con la capa de datos simulada. */
class FacturacionServiceTest {

    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final FacturaRepository facturas = mock(FacturaRepository.class);
    private final Clock reloj = Clock.fixed(Instant.parse("2026-10-05T15:00:00Z"), ZoneOffset.UTC);
    private final UsuarioAutenticado usuario = new UsuarioAutenticado("u-1", "ana", Rol.ADMINISTRADOR);
    private final FacturacionService servicio = new FacturacionService(clientes, productos, facturas,
            new InventarioService(), new AuditoriaService(), reloj);

    private ClienteEntity cliente;
    private ProductoEntity torta;     // 45.000 x stock 10
    private ProductoEntity cupcake;   // 6.000 x stock 20

    private ProductoEntity producto(long id, String nombre, String precio, int stock) {
        ProductoEntity p = new ProductoEntity();
        p.setIdProducto(id);
        p.setCodigoUnico("P-" + id);
        p.setNombre(nombre);
        p.setPrecio(new BigDecimal(precio));
        p.setStock(stock);
        p.setEstado(EstadoRegistro.ACTIVO);
        return p;
    }

    @BeforeEach
    void preparar() {
        cliente = new ClienteEntity();
        cliente.setIdCliente(1L);
        cliente.setNombreCompleto("Ana Pérez");
        cliente.setEstado(EstadoRegistro.ACTIVO);
        torta = producto(1, "Torta", "45000", 10);
        cupcake = producto(2, "Cupcake", "6000", 20);
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(productos.findByIdInParaActualizar(any())).thenReturn(List.of(torta, cupcake));
        when(facturas.siguienteSecuencia()).thenReturn(1L);
        when(facturas.saveAndFlush(any(FacturaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private RegistrarVentaCommand venta(ItemVentaCommand... items) {
        return new RegistrarVentaCommand(1L, List.of(items));
    }

    @Test
    void calculaSubtotalYTotalYDescuentaStock() { // Pruebas 7, 8, 9 y 11
        FacturaResponse f = servicio.registrarVenta(
                venta(new ItemVentaCommand(1L, 2), new ItemVentaCommand(2L, 3)), usuario);

        assertThat(f.detalles().get(0).subtotal()).isEqualByComparingTo("90000");
        assertThat(f.total()).isEqualByComparingTo("108000");
        assertThat(f.numeroFactura()).isEqualTo("FAC-2026-000001");
        assertThat(f.estadoFactura()).isEqualTo("EMITIDA");
        assertThat(f.usuarioAuthcore()).isEqualTo("u-1");
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
        verify(facturas, never()).saveAndFlush(any());
    }

    @Test
    void noPermiteVenderProductoInactivoClienteInactivoNiProductoRepetido() {
        torta.setEstado(EstadoRegistro.INACTIVO);
        assertThatThrownBy(() -> servicio.registrarVenta(venta(new ItemVentaCommand(1L, 1)), usuario))
                .isInstanceOf(ProductoInactivoException.class);

        torta.setEstado(EstadoRegistro.ACTIVO);
        assertThatThrownBy(() -> servicio.registrarVenta(
                venta(new ItemVentaCommand(1L, 1), new ItemVentaCommand(1L, 2)), usuario))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("repetido");

        cliente.setEstado(EstadoRegistro.INACTIVO);
        assertThatThrownBy(() -> servicio.registrarVenta(venta(new ItemVentaCommand(2L, 1)), usuario))
                .isInstanceOf(ClienteInactivoException.class);
        assertThat(cupcake.getStock()).isEqualTo(20);
    }

    @Test
    void anulaFacturaRestauraStockUnaSolaVez() { // Pruebas 12, 13 y 14
        FacturaEntity factura = new FacturaEntity();
        factura.setIdFactura(5L);
        factura.setNumeroFactura("FAC-2026-000005");
        factura.setEstadoFactura(EstadoFactura.EMITIDA);
        DetalleFacturaEntity d = new DetalleFacturaEntity();
        d.setProducto(torta);
        d.setCantidad(4);
        factura.getDetalles().add(d);
        torta.setStock(6);
        when(facturas.findByIdParaActualizar(5L)).thenReturn(Optional.of(factura));
        when(facturas.findByIdConDetalles(5L)).thenReturn(Optional.of(factura));
        when(productos.findByIdInParaActualizar(any())).thenReturn(List.of(torta));
        ClienteEntity c = new ClienteEntity();
        c.setNombreCompleto("Ana");
        factura.setCliente(c);
        factura.setTotal(BigDecimal.ONE);
        factura.setUsuarioAuthcore("u-1");
        factura.setFechaEmision(java.time.LocalDateTime.now());

        servicio.anular(5L, usuario);

        assertThat(factura.getEstadoFactura()).isEqualTo(EstadoFactura.ANULADA);
        assertThat(torta.getStock()).isEqualTo(10);

        assertThatThrownBy(() -> servicio.anular(5L, usuario)).isInstanceOf(FacturaYaAnuladaException.class);
        assertThat(torta.getStock()).isEqualTo(10); // no pasó de 10 a 14
    }
}
