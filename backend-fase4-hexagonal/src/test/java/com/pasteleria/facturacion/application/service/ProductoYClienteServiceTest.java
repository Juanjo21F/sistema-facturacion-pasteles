package com.pasteleria.facturacion.application.service;

import com.pasteleria.facturacion.application.port.in.DatosClienteCommand;
import com.pasteleria.facturacion.application.port.in.DatosProductoCommand;
import com.pasteleria.facturacion.application.port.out.AuditoriaPort;
import com.pasteleria.facturacion.application.port.out.CategoriaRepositoryPort;
import com.pasteleria.facturacion.application.port.out.ClienteRepositoryPort;
import com.pasteleria.facturacion.application.port.out.ProductoRepositoryPort;
import com.pasteleria.facturacion.domain.exception.CodigoProductoDuplicadoException;
import com.pasteleria.facturacion.domain.exception.DocumentoClienteDuplicadoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.model.Categoria;
import com.pasteleria.facturacion.domain.model.Cliente;
import com.pasteleria.facturacion.domain.model.EstadoRegistro;
import com.pasteleria.facturacion.domain.model.Producto;
import java.math.BigDecimal;
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

/** Pruebas de la capa de aplicación aislada: los puertos de salida se simulan con Mockito (sin Spring ni base de datos). */
class ProductoYClienteServiceTest {

    private final ProductoRepositoryPort productos = mock(ProductoRepositoryPort.class);
    private final CategoriaRepositoryPort categorias = mock(CategoriaRepositoryPort.class);
    private final ClienteRepositoryPort clientes = mock(ClienteRepositoryPort.class);
    private final AuditoriaPort auditoria = mock(AuditoriaPort.class);

    private final ProductoService productoService = new ProductoService(productos, categorias, auditoria);
    private final ClienteService clienteService = new ClienteService(clientes, auditoria);

    @BeforeEach
    void preparar() {
        when(categorias.buscarPorId(1L)).thenReturn(Optional.of(new Categoria(1L, "Tortas")));
        when(productos.guardar(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            return new Producto(10L, p.getCodigoUnico(), p.getNombre(), p.getCategoria(), p.getPrecio(), p.getStock(), p.getEstado());
        });
        when(clientes.guardar(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            return new Cliente(20L, c.getDocumentoIdentidad(), c.getNombreCompleto(), c.getTelefono(), c.getCorreo(), c.getEstado());
        });
    }

    private DatosProductoCommand producto(String codigo, String precio, int stock) {
        return new DatosProductoCommand(codigo, "Torta", 1L, new BigDecimal(precio), stock);
    }

    @Test
    void creaProductoYLoGuarda() { // Prueba 1
        Producto creado = productoService.crear(producto("TOR-1", "45000", 5));

        assertThat(creado.getId()).isEqualTo(10L);
        assertThat(creado.getPrecio()).isEqualByComparingTo("45000");
        assertThat(creado.getEstado()).isEqualTo(EstadoRegistro.ACTIVO);
        assertThat(creado.getCategoria().nombre()).isEqualTo("Tortas");
    }

    @Test
    void rechazaPrecioInvalidoYStockNegativo() { // Pruebas 2 y 3
        assertThatThrownBy(() -> productoService.crear(producto("A", "0", 5)))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("precio");
        assertThatThrownBy(() -> productoService.crear(producto("B", "-5", 5)))
                .isInstanceOf(ReglaDeNegocioException.class);
        assertThatThrownBy(() -> productoService.crear(producto("C", "10", -1)))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("stock");
        verify(productos, never()).guardar(any());
    }

    @Test
    void evitaCodigoDeProductoDuplicado() { // Prueba 4
        when(productos.buscarPorCodigo("TOR-1")).thenReturn(Optional.of(mock(Producto.class)));

        assertThatThrownBy(() -> productoService.crear(producto("TOR-1", "30000", 2)))
                .isInstanceOf(CodigoProductoDuplicadoException.class);
    }

    @Test
    void creaCliente() { // Prueba 5
        Cliente c = clienteService.crear(new DatosClienteCommand("123", "Ana Pérez", "300", "ana@mail.com"));

        assertThat(c.getId()).isEqualTo(20L);
        assertThat(c.getEstado()).isEqualTo(EstadoRegistro.ACTIVO);
    }

    @Test
    void evitaDocumentoDeClienteDuplicado() { // Prueba 6
        when(clientes.buscarPorDocumento("123")).thenReturn(Optional.of(mock(Cliente.class)));

        assertThatThrownBy(() -> clienteService.crear(new DatosClienteCommand("123", "Otra", "301", "o@mail.com")))
                .isInstanceOf(DocumentoClienteDuplicadoException.class);
    }
}
