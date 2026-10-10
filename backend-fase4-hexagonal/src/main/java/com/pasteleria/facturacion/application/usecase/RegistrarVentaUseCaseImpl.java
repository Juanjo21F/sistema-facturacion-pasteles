package com.pasteleria.facturacion.application.usecase;

import com.pasteleria.facturacion.application.service.AuditoriaService;
import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.domain.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.port.input.RegistrarVentaUseCase;
import com.pasteleria.facturacion.domain.port.input.command.ItemVentaCommand;
import com.pasteleria.facturacion.domain.port.input.command.RegistrarVentaCommand;
import com.pasteleria.facturacion.domain.port.output.ClienteRepositoryPort;
import com.pasteleria.facturacion.domain.port.output.FacturaRepositoryPort;
import com.pasteleria.facturacion.domain.port.output.ProductoRepositoryPort;
import com.pasteleria.facturacion.domain.service.FacturacionDomainService;
import com.pasteleria.facturacion.domain.valueobject.LineaVenta;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orquesta la venta dentro de UNA transacción: si cualquier paso falla se hace rollback de
 * factura, detalles y stock. Las reglas viven en FacturacionDomainService.
 */
@Service
@Transactional
public class RegistrarVentaUseCaseImpl implements RegistrarVentaUseCase {

    private final ClienteRepositoryPort clientes;
    private final ProductoRepositoryPort productos;
    private final FacturaRepositoryPort facturas;
    private final FacturacionDomainService facturacion;
    private final AuditoriaService auditoria;
    private final Clock reloj;

    public RegistrarVentaUseCaseImpl(ClienteRepositoryPort clientes, ProductoRepositoryPort productos,
                                     FacturaRepositoryPort facturas, FacturacionDomainService facturacion,
                                     AuditoriaService auditoria, Clock reloj) {
        this.clientes = clientes;
        this.productos = productos;
        this.facturas = facturas;
        this.facturacion = facturacion;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    @Override
    public Factura ejecutar(RegistrarVentaCommand comando, UsuarioAutenticado usuario) {
        if (comando.items() == null || comando.items().isEmpty()) {
            throw new ReglaDeNegocioException("La factura debe tener al menos un producto");
        }
        Cliente cliente = clientes.buscarPorId(comando.idCliente())
                .orElseThrow(() -> new ClienteNoEncontradoException(comando.idCliente()));

        Map<Long, Producto> productosBloqueados = cargarProductos(comando.items());
        List<LineaVenta> lineas = comando.items().stream()
                .map(item -> new LineaVenta(buscar(productosBloqueados, item.idProducto()), item.cantidad()))
                .toList();

        Factura factura = facturacion.emitirFactura(cliente, lineas, usuario,
                facturas.siguienteSecuencia(), LocalDateTime.now(reloj));

        productosBloqueados.values().forEach(productos::guardar); // stock ya descontado en memoria
        Factura guardada = facturas.guardar(factura);

        auditoria.registrar(usuario, "REGISTRAR_VENTA",
                "factura=" + guardada.getNumeroFactura() + " total=" + guardada.getTotal());
        return guardada;
    }

    private Map<Long, Producto> cargarProductos(List<ItemVentaCommand> items) {
        List<Long> ids = items.stream().map(ItemVentaCommand::idProducto).distinct().toList();
        return productos.buscarPorIdsConBloqueo(ids).stream()
                .collect(Collectors.toMap(Producto::getIdProducto, Function.identity()));
    }

    private Producto buscar(Map<Long, Producto> productosPorId, Long idProducto) {
        Producto producto = productosPorId.get(idProducto);
        if (producto == null) {
            throw new ProductoNoEncontradoException(idProducto);
        }
        return producto;
    }
}
