package com.pasteleria.facturacion.application.service;

import com.pasteleria.facturacion.application.port.in.FacturacionUseCase;
import com.pasteleria.facturacion.application.port.in.ItemVentaCommand;
import com.pasteleria.facturacion.application.port.in.RegistrarVentaCommand;
import com.pasteleria.facturacion.application.port.out.AuditoriaPort;
import com.pasteleria.facturacion.application.port.out.ClienteRepositoryPort;
import com.pasteleria.facturacion.application.port.out.FacturaRepositoryPort;
import com.pasteleria.facturacion.application.port.out.ProductoRepositoryPort;
import com.pasteleria.facturacion.domain.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.domain.exception.FacturaNoEncontradaException;
import com.pasteleria.facturacion.domain.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.model.Cliente;
import com.pasteleria.facturacion.domain.model.DetalleFactura;
import com.pasteleria.facturacion.domain.model.Factura;
import com.pasteleria.facturacion.domain.model.Producto;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aplicación: registra y anula ventas en UNA transacción (si algo falla, rollback de factura, detalles y stock).
 * Coordina clientes, inventario y numeración mediante puertos; las reglas viven en el dominio.
 */
@Transactional
public class FacturacionService implements FacturacionUseCase {

    private final ClienteRepositoryPort clientes;
    private final ProductoRepositoryPort productos;
    private final FacturaRepositoryPort facturas;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public FacturacionService(ClienteRepositoryPort clientes, ProductoRepositoryPort productos,
                              FacturaRepositoryPort facturas, AuditoriaPort auditoria, Clock reloj) {
        this.clientes = clientes;
        this.productos = productos;
        this.facturas = facturas;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    @Override
    public Factura registrarVenta(RegistrarVentaCommand comando, UsuarioAutenticado usuario) {
        if (comando.items() == null || comando.items().isEmpty()) {
            throw new ReglaDeNegocioException("La factura debe tener al menos un producto");
        }
        Cliente cliente = clientes.buscarPorId(comando.idCliente())
                .orElseThrow(() -> new ClienteNoEncontradoException(comando.idCliente()));
        Map<Long, Producto> bloqueados = cargarProductosBloqueados(
                comando.items().stream().map(ItemVentaCommand::idProducto).distinct().toList());

        // 1) validar todo antes de modificar nada
        Set<Long> vistos = new HashSet<>();
        for (ItemVentaCommand item : comando.items()) {
            Producto producto = buscar(bloqueados, item.idProducto());
            producto.validarDisponibleParaVenta();
            if (!vistos.add(item.idProducto())) {
                throw new ReglaDeNegocioException(
                        "El producto \"" + producto.getNombre() + "\" está repetido en la factura");
            }
        }
        cliente.validarActivoParaVenta();
        comando.items().forEach(i -> bloqueados.get(i.idProducto()).validarStock(i.cantidad()));

        // 2) construir la factura (el total lo calcula el dominio) y descontar el stock
        LocalDateTime fecha = LocalDateTime.now(reloj);
        List<DetalleFactura> detalles = new ArrayList<>();
        for (ItemVentaCommand item : comando.items()) {
            Producto producto = bloqueados.get(item.idProducto());
            detalles.add(DetalleFactura.de(producto, item.cantidad()));
            producto.descontar(item.cantidad());
        }
        Factura factura = Factura.emitir(
                String.format("FAC-%d-%06d", fecha.getYear(), facturas.siguienteSecuencia()),
                fecha, cliente, usuario.id(), detalles);

        productos.guardarTodos(bloqueados.values());
        Factura guardada = facturas.guardar(factura);
        auditoria.registrar(usuario, "REGISTRAR_VENTA",
                "factura=" + guardada.getNumeroFactura() + " total=" + guardada.getTotal());
        return guardada;
    }

    @Override
    public Factura anular(Long idFactura, UsuarioAutenticado usuario) {
        facturas.buscarPorIdParaActualizar(idFactura)       // bloquea la factura: evita doble restauración de stock
                .orElseThrow(() -> new FacturaNoEncontradaException(idFactura));
        Factura factura = facturas.buscarPorId(idFactura)
                .orElseThrow(() -> new FacturaNoEncontradaException(idFactura));
        factura.anular();   // el dominio rechaza una segunda anulación (FacturaYaAnuladaException)
        Map<Long, Producto> bloqueados = cargarProductosBloqueados(factura.getDetalles().stream()
                .map(d -> d.producto().getId()).distinct().toList());
        for (DetalleFactura detalle : factura.getDetalles()) {
            buscar(bloqueados, detalle.producto().getId()).restaurar(detalle.cantidad());
        }
        productos.guardarTodos(bloqueados.values());
        Factura guardada = facturas.guardar(factura);
        auditoria.registrar(usuario, "ANULAR_FACTURA", "factura=" + guardada.getNumeroFactura());
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Factura obtenerPorId(Long idFactura) {
        return facturas.buscarPorId(idFactura).orElseThrow(() -> new FacturaNoEncontradaException(idFactura));
    }

    private Map<Long, Producto> cargarProductosBloqueados(List<Long> ids) {
        return productos.buscarPorIdsParaActualizar(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
    }

    private Producto buscar(Map<Long, Producto> porId, Long idProducto) {
        Producto producto = porId.get(idProducto);
        if (producto == null) {
            throw new ProductoNoEncontradoException(idProducto);
        }
        return producto;
    }
}
