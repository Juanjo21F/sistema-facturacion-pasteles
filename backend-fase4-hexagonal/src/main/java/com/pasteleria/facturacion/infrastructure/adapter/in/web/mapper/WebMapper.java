package com.pasteleria.facturacion.infrastructure.adapter.in.web.mapper;

import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.ActualizarClienteRequest;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.ActualizarProductoRequest;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.CrearClienteRequest;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.CrearFacturaRequest;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.request.CrearProductoRequest;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.ClienteResponse;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.DetalleFacturaResponse;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.FacturaResponse;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.ProductoResponse;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.ReporteMensualResponse;
import com.pasteleria.facturacion.infrastructure.adapter.in.web.response.ReporteProductoResponse;
import com.pasteleria.facturacion.application.port.in.DatosClienteCommand;
import com.pasteleria.facturacion.application.port.in.DatosProductoCommand;
import com.pasteleria.facturacion.application.port.in.ItemVentaCommand;
import com.pasteleria.facturacion.application.port.in.RegistrarVentaCommand;
import com.pasteleria.facturacion.domain.model.Cliente;
import com.pasteleria.facturacion.domain.model.Factura;
import com.pasteleria.facturacion.domain.model.Producto;
import com.pasteleria.facturacion.domain.model.ReporteMensual;

import java.util.List;

/** Adaptador web: traduce requests HTTP → comandos de aplicación y modelo de dominio → respuestas JSON. */
public final class WebMapper {

    private WebMapper() {
    }

    // ---- entrada ----

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

    // ---- salida ----

    public static ProductoResponse aRespuesta(Producto p) {
        return new ProductoResponse(p.getId(), p.getCodigoUnico(), p.getNombre(), p.getCategoria().id(),
                p.getCategoria().nombre(), p.getPrecio(), p.getStock(), p.getEstado().name());
    }

    public static ClienteResponse aRespuesta(Cliente c) {
        return new ClienteResponse(c.getId(), c.getDocumentoIdentidad(), c.getNombreCompleto(),
                c.getTelefono(), c.getCorreo(), c.getEstado().name());
    }

    public static FacturaResponse aRespuesta(Factura f) {
        List<DetalleFacturaResponse> detalles = f.getDetalles().stream()
                .map(d -> new DetalleFacturaResponse(d.id(), d.producto().getId(), d.producto().getNombre(),
                        d.cantidad(), d.precioUnitario(), d.subtotal()))
                .toList();
        return new FacturaResponse(f.getId(), f.getNumeroFactura(), f.getFechaEmision(), f.getCliente().getId(),
                f.getCliente().getNombreCompleto(), f.getUsuarioAuthcore(), f.getTotal(), f.getEstado().name(), detalles);
    }

    public static ReporteMensualResponse aRespuesta(ReporteMensual r) {
        List<ReporteProductoResponse> productos = r.productos().stream()
                .map(f -> new ReporteProductoResponse(f.productoId(), f.codigoProducto(), f.nombreProducto(),
                        f.cantidadVendida(), f.totalIngresos()))
                .toList();
        return new ReporteMensualResponse(r.mes(), r.anio(), productos);
    }
}
