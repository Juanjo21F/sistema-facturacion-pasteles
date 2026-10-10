package com.pasteleria.facturacion.business.service;

import com.pasteleria.facturacion.business.dto.FacturaResponse;
import com.pasteleria.facturacion.business.dto.ItemVentaCommand;
import com.pasteleria.facturacion.business.dto.RegistrarVentaCommand;
import com.pasteleria.facturacion.business.exception.ClienteInactivoException;
import com.pasteleria.facturacion.business.exception.ClienteNoEncontradoException;
import com.pasteleria.facturacion.business.exception.FacturaNoEncontradaException;
import com.pasteleria.facturacion.business.exception.FacturaYaAnuladaException;
import com.pasteleria.facturacion.business.exception.ProductoNoEncontradoException;
import com.pasteleria.facturacion.business.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.business.mapper.EntityMapper;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Capa de negocio: registra y anula ventas en UNA transacción (si algo falla, rollback de factura,
 * detalles y stock). Coordina clientes, inventario y numeración.
 */
@Service
@Transactional
public class FacturacionService {

    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final FacturaRepository facturas;
    private final InventarioService inventario;
    private final AuditoriaService auditoria;
    private final Clock reloj;

    public FacturacionService(ClienteRepository clientes, ProductoRepository productos, FacturaRepository facturas,
                              InventarioService inventario, AuditoriaService auditoria, Clock reloj) {
        this.clientes = clientes;
        this.productos = productos;
        this.facturas = facturas;
        this.inventario = inventario;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public FacturaResponse registrarVenta(RegistrarVentaCommand comando, UsuarioAutenticado usuario) {
        if (comando.items() == null || comando.items().isEmpty()) {
            throw new ReglaDeNegocioException("La factura debe tener al menos un producto");
        }
        ClienteEntity cliente = clientes.findById(comando.idCliente())
                .orElseThrow(() -> new ClienteNoEncontradoException(comando.idCliente()));
        Map<Long, ProductoEntity> bloqueados = cargarProductosBloqueados(
                comando.items().stream().map(ItemVentaCommand::idProducto).distinct().toList());

        // 1) validar todo antes de modificar nada
        Set<Long> vistos = new HashSet<>();
        for (ItemVentaCommand item : comando.items()) {
            ProductoEntity producto = buscar(bloqueados, item.idProducto());
            inventario.validarDisponibleParaVenta(producto);
            if (!vistos.add(item.idProducto())) {
                throw new ReglaDeNegocioException(
                        "El producto \"" + producto.getNombre() + "\" está repetido en la factura");
            }
        }
        if (cliente.getEstado() != EstadoRegistro.ACTIVO) {
            throw new ClienteInactivoException(cliente.getNombreCompleto());
        }
        comando.items().forEach(i -> inventario.validarStock(bloqueados.get(i.idProducto()), i.cantidad()));

        // 2) construir la factura (el total lo calcula el backend) y descontar el stock
        LocalDateTime fecha = LocalDateTime.now(reloj);
        FacturaEntity factura = new FacturaEntity();
        factura.setNumeroFactura(String.format("FAC-%d-%06d", fecha.getYear(), facturas.siguienteSecuencia()));
        factura.setFechaEmision(fecha);
        factura.setCliente(cliente);
        factura.setUsuarioAuthcore(usuario.id());
        factura.setEstadoFactura(EstadoFactura.EMITIDA);
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVentaCommand item : comando.items()) {
            ProductoEntity producto = bloqueados.get(item.idProducto());
            DetalleFacturaEntity detalle = new DetalleFacturaEntity();
            detalle.setFactura(factura);
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
            factura.getDetalles().add(detalle);
            total = total.add(detalle.getSubtotal());
            inventario.descontar(producto, item.cantidad());
        }
        factura.setTotal(total);

        productos.saveAll(bloqueados.values());
        FacturaEntity guardada = facturas.saveAndFlush(factura);
        auditoria.registrar(usuario, "REGISTRAR_VENTA",
                "factura=" + guardada.getNumeroFactura() + " total=" + guardada.getTotal());
        return EntityMapper.aRespuesta(guardada);
    }

    public FacturaResponse anular(Long idFactura, UsuarioAutenticado usuario) {
        facturas.findByIdParaActualizar(idFactura)       // bloquea la factura: evita doble restauración de stock
                .orElseThrow(() -> new FacturaNoEncontradaException(idFactura));
        FacturaEntity factura = facturas.findByIdConDetalles(idFactura)
                .orElseThrow(() -> new FacturaNoEncontradaException(idFactura));
        if (factura.getEstadoFactura() == EstadoFactura.ANULADA) {
            throw new FacturaYaAnuladaException(factura.getNumeroFactura());
        }
        Map<Long, ProductoEntity> bloqueados = cargarProductosBloqueados(factura.getDetalles().stream()
                .map(d -> d.getProducto().getIdProducto()).distinct().toList());
        for (DetalleFacturaEntity detalle : factura.getDetalles()) {
            inventario.restaurar(buscar(bloqueados, detalle.getProducto().getIdProducto()), detalle.getCantidad());
        }
        factura.setEstadoFactura(EstadoFactura.ANULADA);
        productos.saveAll(bloqueados.values());
        FacturaEntity guardada = facturas.saveAndFlush(factura);
        auditoria.registrar(usuario, "ANULAR_FACTURA", "factura=" + guardada.getNumeroFactura());
        return EntityMapper.aRespuesta(guardada);
    }

    @Transactional(readOnly = true)
    public FacturaResponse obtenerPorId(Long idFactura) {
        return EntityMapper.aRespuesta(facturas.findByIdConDetalles(idFactura)
                .orElseThrow(() -> new FacturaNoEncontradaException(idFactura)));
    }

    private Map<Long, ProductoEntity> cargarProductosBloqueados(List<Long> ids) {
        return productos.findByIdInParaActualizar(ids).stream()
                .collect(Collectors.toMap(ProductoEntity::getIdProducto, Function.identity()));
    }

    private ProductoEntity buscar(Map<Long, ProductoEntity> porId, Long idProducto) {
        ProductoEntity producto = porId.get(idProducto);
        if (producto == null) {
            throw new ProductoNoEncontradoException(idProducto);
        }
        return producto;
    }
}
