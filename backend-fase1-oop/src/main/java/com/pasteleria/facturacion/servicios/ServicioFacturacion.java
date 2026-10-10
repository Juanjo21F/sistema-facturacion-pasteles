package com.pasteleria.facturacion.servicios;

import com.pasteleria.facturacion.modelo.entidades.Cliente;
import com.pasteleria.facturacion.modelo.entidades.Factura;
import com.pasteleria.facturacion.modelo.entidades.LineaVenta;
import com.pasteleria.facturacion.modelo.entidades.Producto;
import com.pasteleria.facturacion.modelo.excepciones.ClienteNoEncontradoException;
import com.pasteleria.facturacion.modelo.excepciones.FacturaNoEncontradaException;
import com.pasteleria.facturacion.modelo.excepciones.ProductoNoEncontradoException;
import com.pasteleria.facturacion.modelo.excepciones.ReglaDeNegocioException;
import com.pasteleria.facturacion.modelo.reglas.ContextoVenta;
import com.pasteleria.facturacion.modelo.reglas.ReglaDeVenta;
import com.pasteleria.facturacion.repositorios.ClienteRepositorio;
import com.pasteleria.facturacion.repositorios.FacturaRepositorio;
import com.pasteleria.facturacion.repositorios.ProductoRepositorio;
import com.pasteleria.facturacion.seguridad.UsuarioAutenticado;
import com.pasteleria.facturacion.servicios.comandos.ItemVentaCommand;
import com.pasteleria.facturacion.servicios.comandos.RegistrarVentaCommand;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emite y anula facturas en UNA transacción. Las reglas de venta son objetos intercambiables
 * ({@link ReglaDeVenta}) que Spring inyecta en orden; aquí solo se recorren (polimorfismo).
 */
@Service
@Transactional
public class ServicioFacturacion extends ServicioBase<Factura> {

    private final ClienteRepositorio clientes;
    private final ProductoRepositorio productos;
    private final FacturaRepositorio facturas;
    private final List<ReglaDeVenta> reglas;
    private final ServicioAuditoria auditoria;
    private final Clock reloj;

    public ServicioFacturacion(ClienteRepositorio clientes, ProductoRepositorio productos, FacturaRepositorio facturas,
                               List<ReglaDeVenta> reglas, ServicioAuditoria auditoria, Clock reloj) {
        this.clientes = clientes;
        this.productos = productos;
        this.facturas = facturas;
        this.reglas = reglas;
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
                .map(i -> new LineaVenta(buscarProducto(bloqueados, i.idProducto()), i.cantidad()))
                .toList();

        ContextoVenta contexto = new ContextoVenta(cliente, lineas);
        reglas.forEach(regla -> regla.validar(contexto));          // polimorfismo: cada regla valida a su modo

        LocalDateTime fecha = LocalDateTime.now(reloj);
        Factura factura = Factura.emitir(Factura.generarNumero(facturas.siguienteSecuencia(), fecha),
                fecha, cliente, usuario.id(), lineas);
        lineas.forEach(l -> l.producto().descontarStock(l.cantidad()));
        productos.saveAll(bloqueados.values());
        Factura guardada = guardar(factura);
        auditoria.registrar(usuario, "REGISTRAR_VENTA",
                "factura=" + guardada.getNumeroFactura() + " total=" + guardada.getTotal());
        return guardada;
    }

    public Factura anular(Long idFactura, UsuarioAutenticado usuario) {
        facturas.findByIdParaActualizar(idFactura)       // bloquea la factura: evita doble restauración de stock
                .orElseThrow(() -> noEncontrado(idFactura));
        Factura factura = obtenerPorId(idFactura);
        Map<Long, Producto> bloqueados = cargarProductosBloqueados(factura.getDetalles().stream()
                .map(d -> d.getProducto().getId()).distinct().toList());

        factura.anular();                                          // falla ANTES de tocar el inventario si ya estaba anulada
        factura.getDetalles().forEach(d ->
                buscarProducto(bloqueados, d.getProducto().getId()).restaurarStock(d.getCantidad()));
        productos.saveAll(bloqueados.values());
        Factura guardada = guardar(factura);
        auditoria.registrar(usuario, "ANULAR_FACTURA", "factura=" + guardada.getNumeroFactura());
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Factura obtenerPorId(Long id) {
        return super.obtenerPorId(id);
    }

    @Override
    protected Optional<Factura> buscar(Long id) {
        return facturas.findByIdConDetalles(id);
    }

    @Override
    protected RuntimeException noEncontrado(Long id) {
        return new FacturaNoEncontradaException(id);
    }

    @Override
    protected Factura guardar(Factura factura) {
        return facturas.saveAndFlush(factura);
    }

    private Map<Long, Producto> cargarProductosBloqueados(List<Long> ids) {
        return productos.findByIdInParaActualizar(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
    }

    private Producto buscarProducto(Map<Long, Producto> porId, Long idProducto) {
        Producto producto = porId.get(idProducto);
        if (producto == null) {
            throw new ProductoNoEncontradoException(idProducto);
        }
        return producto;
    }
}
