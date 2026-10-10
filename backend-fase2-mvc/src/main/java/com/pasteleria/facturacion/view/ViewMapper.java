package com.pasteleria.facturacion.view;

import com.pasteleria.facturacion.model.entity.Cliente;
import com.pasteleria.facturacion.model.entity.DetalleFactura;
import com.pasteleria.facturacion.model.entity.Factura;
import com.pasteleria.facturacion.model.entity.Producto;
import com.pasteleria.facturacion.model.report.ReporteProductoVendido;
import com.pasteleria.facturacion.model.service.command.DatosClienteCommand;
import com.pasteleria.facturacion.model.service.command.DatosProductoCommand;
import com.pasteleria.facturacion.model.service.command.ItemVentaCommand;
import com.pasteleria.facturacion.model.service.command.RegistrarVentaCommand;
import com.pasteleria.facturacion.view.request.ActualizarClienteRequest;
import com.pasteleria.facturacion.view.request.ActualizarProductoRequest;
import com.pasteleria.facturacion.view.request.CrearClienteRequest;
import com.pasteleria.facturacion.view.request.CrearFacturaRequest;
import com.pasteleria.facturacion.view.request.CrearProductoRequest;
import com.pasteleria.facturacion.view.response.ClienteResponse;
import com.pasteleria.facturacion.view.response.DetalleFacturaResponse;
import com.pasteleria.facturacion.view.response.FacturaResponse;
import com.pasteleria.facturacion.view.response.ProductoResponse;
import com.pasteleria.facturacion.view.response.ReporteMensualResponse;
import com.pasteleria.facturacion.view.response.ReporteProductoResponse;

import java.util.List;

/** Convierte entre DTOs de la API y comandos/entidades de dominio. Sin lógica de negocio. */
public final class ViewMapper {

    private ViewMapper() {
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
        List<DetalleFacturaResponse> detalles = f.getDetalles().stream().map(ViewMapper::aRespuesta).toList();
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
