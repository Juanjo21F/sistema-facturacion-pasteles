package com.pasteleria.facturacion.business.service;

import com.pasteleria.facturacion.business.dto.ClienteResponse;
import com.pasteleria.facturacion.business.dto.DatosClienteCommand;
import com.pasteleria.facturacion.business.dto.DatosProductoCommand;
import com.pasteleria.facturacion.business.dto.ProductoResponse;
import com.pasteleria.facturacion.business.exception.CodigoProductoDuplicadoException;
import com.pasteleria.facturacion.business.exception.DocumentoClienteDuplicadoException;
import com.pasteleria.facturacion.business.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.dataaccess.entity.CategoriaEntity;
import com.pasteleria.facturacion.dataaccess.entity.ClienteEntity;
import com.pasteleria.facturacion.dataaccess.entity.ProductoEntity;
import com.pasteleria.facturacion.dataaccess.repository.CategoriaRepository;
import com.pasteleria.facturacion.dataaccess.repository.ClienteRepository;
import com.pasteleria.facturacion.dataaccess.repository.ProductoRepository;
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

/** Pruebas de la capa de negocio aislada: los repositorios (capa de datos) se simulan con Mockito. */
class ProductoYClienteServiceTest {

    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final CategoriaRepository categorias = mock(CategoriaRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final AuditoriaService auditoria = new AuditoriaService();

    private final ProductoService productoService = new ProductoService(productos, categorias, auditoria);
    private final ClienteService clienteService = new ClienteService(clientes, auditoria);

    @BeforeEach
    void preparar() {
        CategoriaEntity tortas = new CategoriaEntity();
        tortas.setIdCategoria(1L);
        tortas.setNombre("Tortas");
        when(categorias.findById(1L)).thenReturn(Optional.of(tortas));
        when(productos.saveAndFlush(any(ProductoEntity.class))).thenAnswer(inv -> {
            ProductoEntity p = inv.getArgument(0);
            p.setIdProducto(10L);
            return p;
        });
        when(clientes.saveAndFlush(any(ClienteEntity.class))).thenAnswer(inv -> {
            ClienteEntity c = inv.getArgument(0);
            c.setIdCliente(20L);
            return c;
        });
    }

    private DatosProductoCommand producto(String codigo, String precio, int stock) {
        return new DatosProductoCommand(codigo, "Torta", 1L, new BigDecimal(precio), stock);
    }

    @Test
    void creaProductoYLoGuarda() { // Prueba 1
        ProductoResponse creado = productoService.crear(producto("TOR-1", "45000", 5));

        assertThat(creado.idProducto()).isEqualTo(10L);
        assertThat(creado.precio()).isEqualByComparingTo("45000");
        assertThat(creado.estado()).isEqualTo("ACTIVO");
        assertThat(creado.categoria()).isEqualTo("Tortas");
    }

    @Test
    void rechazaPrecioInvalidoYStockNegativo() { // Pruebas 2 y 3
        assertThatThrownBy(() -> productoService.crear(producto("A", "0", 5)))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("precio");
        assertThatThrownBy(() -> productoService.crear(producto("B", "-5", 5)))
                .isInstanceOf(ReglaDeNegocioException.class);
        assertThatThrownBy(() -> productoService.crear(producto("C", "10", -1)))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("stock");
        verify(productos, never()).saveAndFlush(any());
    }

    @Test
    void evitaCodigoDeProductoDuplicado() { // Prueba 4
        when(productos.findByCodigoConCategoria("TOR-1")).thenReturn(Optional.of(new ProductoEntity()));

        assertThatThrownBy(() -> productoService.crear(producto("TOR-1", "30000", 2)))
                .isInstanceOf(CodigoProductoDuplicadoException.class);
    }

    @Test
    void creaCliente() { // Prueba 5
        ClienteResponse c = clienteService.crear(new DatosClienteCommand("123", "Ana Pérez", "300", "ana@mail.com"));

        assertThat(c.idCliente()).isEqualTo(20L);
        assertThat(c.estado()).isEqualTo("ACTIVO");
    }

    @Test
    void evitaDocumentoDeClienteDuplicado() { // Prueba 6
        when(clientes.findByDocumentoIdentidad("123")).thenReturn(Optional.of(new ClienteEntity()));

        assertThatThrownBy(() -> clienteService.crear(new DatosClienteCommand("123", "Otra", "301", "o@mail.com")))
                .isInstanceOf(DocumentoClienteDuplicadoException.class);
    }
}
