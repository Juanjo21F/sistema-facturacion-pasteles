package com.pasteleria.facturacion.presentation.dto;

import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.entity.DetalleFactura;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.port.input.command.DatosClienteCommand;
import com.pasteleria.facturacion.domain.port.input.command.DatosProductoCommand;
import com.pasteleria.facturacion.domain.port.input.command.ItemVentaCommand;
import com.pasteleria.facturacion.domain.port.input.command.RegistrarVentaCommand;
import com.pasteleria.facturacion.domain.valueobject.ReporteProductoVendido;

import java.util.List;

/** Convierte entre DTOs de la API y comandos/entidades de dominio. Sin lógica de negocio. */
public final class ApiMapper {

    private ApiMapper() {
    }

    // ---- Request -> Command ----
    public static DatosProductoCommand aComando(CrearProductoRequest r) {
        return new DatosProductoCommand(r.codigoUnico(), r.nombre(), r.idCategoria(), r.precio(), r.stock());
    }

    public static DatosProductoCommand aComando(ActualizarProductoRequest r) {
        return new DatosProductoCommand(r.codigoUnico(), r.nombre(), r.idCategoria(), r.precio(), r.stock());
    }

    public static DatosClienteCommand aComando(CrearClienteRequest r) {
        return new DatosClienteCommand(r.documentoIdentidad(), r.nombreCompleto(), r.telefono(), r.correo());
    }

    public static DatosClienteCommand aComando(ActualizarClienteRequest r) {
        return new DatosClienteCommand(r.documentoIdentidad(), r.nombreCompleto(), r.telefono(), r.correo());
    }

    public static RegistrarVentaCommand aComando(CrearFacturaRequest r) {
        List<ItemVentaCommand> items = r.detalles().stream()
                .map(d -> new ItemVentaCommand(d.idProducto(), d.cantidad()))
                .toList();
        return new RegistrarVentaCommand(r.idCliente(), items);
    }

    // ---- Dominio -> Response ----
    public static ProductoResponse aRespuesta(Producto p) {
        return new ProductoResponse(p.getIdProducto(), p.getCodigoUnico(), p.getNombre(),
                p.getCategoria().getIdCategoria(), p.getCategoria().getNombre(),
                p.getPrecio(), p.getStock(), p.getEstado().name());
    }

    public static ClienteResponse aRespuesta(Cliente c) {
        return new ClienteResponse(c.getIdCliente(), c.getDocumentoIdentidad(), c.getNombreCompleto(),
                c.getTelefono(), c.getCorreo(), c.getEstado().name());
    }

    public static FacturaResponse aRespuesta(Factura f) {
        List<DetalleFacturaResponse> detalles = f.getDetalles().stream().map(ApiMapper::aRespuesta).toList();
        return new FacturaResponse(f.getIdFactura(), f.getNumeroFactura(), f.getFechaEmision(),
                f.getCliente().getIdCliente(), f.getCliente().getNombreCompleto(), f.getUsuarioAuthcore(),
                f.getTotal(), f.getEstadoFactura().name(), detalles);
    }

    private static DetalleFacturaResponse aRespuesta(DetalleFactura d) {
        return new DetalleFacturaResponse(d.getIdDetalle(), d.getProducto().getIdProducto(),
                d.getProducto().getNombre(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
    }

    public static ReporteMensualResponse aRespuesta(int mes, int anio, List<ReporteProductoVendido> filas) {
        List<ReporteProductoResponse> productos = filas.stream()
                .map(f -> new ReporteProductoResponse(f.productoId(), f.codigoProducto(), f.nombreProducto(),
                        f.cantidadVendida(), f.totalIngresos()))
                .toList();
        return new ReporteMensualResponse(mes, anio, productos);
    }
}
