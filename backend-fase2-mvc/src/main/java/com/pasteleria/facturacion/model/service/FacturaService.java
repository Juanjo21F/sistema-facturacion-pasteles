package com.pasteleria.facturacion.model.service;

import com.pasteleria.facturacion.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.exception.FacturaNoEncontradaException;
import com.pasteleria.facturacion.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.model.entity.Cliente;
import com.pasteleria.facturacion.model.entity.Factura;
import com.pasteleria.facturacion.model.entity.LineaVenta;
import com.pasteleria.facturacion.model.entity.Producto;
import com.pasteleria.facturacion.model.repository.ClienteRepository;
import com.pasteleria.facturacion.model.repository.FacturaRepository;
import com.pasteleria.facturacion.model.repository.ProductoRepository;
import com.pasteleria.facturacion.model.security.UsuarioAutenticado;
import com.pasteleria.facturacion.model.service.command.ItemVentaCommand;
import com.pasteleria.facturacion.model.service.command.RegistrarVentaCommand;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Modelo: registra y anula ventas en UNA transacción (si algo falla, rollback de factura, detalles y stock).
 * Las reglas viven en {@link Factura}; aquí solo se orquesta.
 */
@Service
@Transactional
public class FacturaService {

    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final FacturaRepository facturas;
    private final AuditoriaService auditoria;
    private final Clock reloj;

    public FacturaService(ClienteRepository clientes, ProductoRepository productos, FacturaRepository facturas,
                          AuditoriaService auditoria, Clock reloj) {
        this.clientes = clientes;
        this.productos = productos;
        this.facturas = facturas;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public Factura registrarVenta(RegistrarVentaCommand comando, UsuarioAutenticado usuario) {
        if (comando.items() == null || comando.items().isEmpty()) {
            throw new ReglaDeNegocioException("La factura debe tener al menos un producto");
        }
        Cliente cliente = clientes.findById(comando.idCliente())
                .orElseThrow(() -> new ClienteNoEncontradoException(comando.idCliente()));
        Map<Long, Producto> bloqueados = cargarProductosBloqueados(
                comando.items().stream().map(ItemVentaCommand::idProducto).distinct().toList());
        List<LineaVenta> lineas = comando.items().stream()
                .map(i -> new LineaVenta(buscar(bloqueados, i.idProducto()), i.cantidad()))
                .toList();

        Factura factura = Factura.emitir(cliente, lineas, usuario.id(), facturas.siguienteSecuencia(),
                LocalDateTime.now(reloj));
        productos.saveAll(bloqueados.values());           // stock ya descontado en memoria
        Factura guardada = facturas.saveAndFlush(factura);
        auditoria.registrar(usuario, "REGISTRAR_VENTA",
                "factura=" + guardada.getNumeroFactura() + " total=" + guardada.getTotal());
        return guardada;
    }

    public Factura anular(Long idFactura, UsuarioAutenticado usuario) {
        facturas.findByIdParaActualizar(idFactura)       // bloquea la factura: evita doble restauración de stock
                .orElseThrow(() -> new FacturaNoEncontradaException(idFactura));
        Factura factura = facturas.findByIdConDetalles(idFactura)
                .orElseThrow(() -> new FacturaNoEncontradaException(idFactura));
        List<Long> ids = factura.getDetalles().stream()
                .map(d -> d.getProducto().getIdProducto()).distinct().toList();
        Map<Long, Producto> bloqueados = cargarProductosBloqueados(ids);

        factura.anular(bloqueados);
        productos.saveAll(bloqueados.values());
        Factura guardada = facturas.saveAndFlush(factura);
        auditoria.registrar(usuario, "ANULAR_FACTURA", "factura=" + guardada.getNumeroFactura());
        return guardada;
    }

    @Transactional(readOnly = true)
    public Factura obtenerPorId(Long idFactura) {
        return facturas.findByIdConDetalles(idFactura).orElseThrow(() -> new FacturaNoEncontradaException(idFactura));
    }

    private Map<Long, Producto> cargarProductosBloqueados(List<Long> ids) {
        return productos.findByIdInParaActualizar(ids).stream()
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
