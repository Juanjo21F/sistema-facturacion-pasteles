package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.CodigoProductoDuplicadoException;
import com.pasteleria.facturacion.domain.exception.DocumentoClienteDuplicadoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.port.input.command.DatosClienteCommand;
import com.pasteleria.facturacion.domain.port.input.command.DatosProductoCommand;
import com.pasteleria.facturacion.support.InMemoryCategoriaRepository;
import com.pasteleria.facturacion.support.InMemoryClienteRepository;
import com.pasteleria.facturacion.support.InMemoryProductoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoYClienteUseCasesTest {

    private final InMemoryProductoRepository productos = new InMemoryProductoRepository();
    private final InMemoryClienteRepository clientes = new InMemoryClienteRepository();
    private final CrearProductoUseCaseImpl crearProducto =
            new CrearProductoUseCaseImpl(productos, new InMemoryCategoriaRepository());
    private final CrearClienteUseCaseImpl crearCliente = new CrearClienteUseCaseImpl(clientes);

    private DatosProductoCommand producto(String codigo, String precio, int stock) {
        return new DatosProductoCommand(codigo, "Torta", 1L, new BigDecimal(precio), stock);
    }

    @Test
    void creaProductoYLoGuarda() { // Prueba 1 (a través del caso de uso)
        Producto creado = crearProducto.ejecutar(producto("TOR-1", "45000", 5));

        assertThat(creado.getIdProducto()).isNotNull();
        assertThat(productos.buscarPorCodigo("TOR-1")).isPresent();
    }

    @Test
    void rechazaPrecioInvalidoYStockNegativoEnElCasoDeUso() { // Pruebas 2 y 3
        assertThatThrownBy(() -> crearProducto.ejecutar(producto("A", "0", 5))).isInstanceOf(ReglaDeNegocioException.class);
        assertThatThrownBy(() -> crearProducto.ejecutar(producto("B", "10", -1))).isInstanceOf(ReglaDeNegocioException.class);
        assertThat(productos.buscarTodos(false)).isEmpty();
    }

    @Test
    void evitaCodigoDeProductoDuplicado() { // Prueba 4
        crearProducto.ejecutar(producto("TOR-1", "45000", 5));

        assertThatThrownBy(() -> crearProducto.ejecutar(producto("TOR-1", "30000", 2)))
                .isInstanceOf(CodigoProductoDuplicadoException.class);
    }

    @Test
    void creaCliente() { // Prueba 5
        Cliente c = crearCliente.ejecutar(new DatosClienteCommand("123", "Ana Pérez", "300", "ana@mail.com"));

        assertThat(c.getIdCliente()).isNotNull();
        assertThat(c.estaActivo()).isTrue();
    }

    @Test
    void evitaDocumentoDeClienteDuplicado() { // Prueba 6
        crearCliente.ejecutar(new DatosClienteCommand("123", "Ana Pérez", "300", "ana@mail.com"));

        assertThatThrownBy(() -> crearCliente.ejecutar(new DatosClienteCommand("123", "Otra Persona", "301", "o@mail.com")))
                .isInstanceOf(DocumentoClienteDuplicadoException.class);
    }
}
