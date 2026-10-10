package com.pasteleria.facturacion.api.dto;

import com.pasteleria.facturacion.api.dto.peticiones.ActualizarClienteRequest;
import com.pasteleria.facturacion.api.dto.peticiones.ActualizarProductoRequest;
import com.pasteleria.facturacion.api.dto.peticiones.CrearClienteRequest;
import com.pasteleria.facturacion.api.dto.peticiones.CrearFacturaRequest;
import com.pasteleria.facturacion.api.dto.peticiones.CrearProductoRequest;
import com.pasteleria.facturacion.api.dto.respuestas.ClienteResponse;
import com.pasteleria.facturacion.api.dto.respuestas.DetalleFacturaResponse;
import com.pasteleria.facturacion.api.dto.respuestas.FacturaResponse;
import com.pasteleria.facturacion.api.dto.respuestas.ProductoResponse;
import com.pasteleria.facturacion.api.dto.respuestas.ReporteMensualResponse;
import com.pasteleria.facturacion.api.dto.respuestas.ReporteProductoResponse;
import com.pasteleria.facturacion.modelo.entidades.Cliente;
import com.pasteleria.facturacion.modelo.entidades.DetalleFactura;
import com.pasteleria.facturacion.modelo.entidades.Factura;
import com.pasteleria.facturacion.modelo.entidades.Producto;
import com.pasteleria.facturacion.modelo.reportes.ReporteProductoVendido;
import com.pasteleria.facturacion.servicios.comandos.DatosClienteCommand;
import com.pasteleria.facturacion.servicios.comandos.DatosProductoCommand;
import com.pasteleria.facturacion.servicios.comandos.ItemVentaCommand;
import com.pasteleria.facturacion.servicios.comandos.RegistrarVentaCommand;

import java.util.List;

/** Convierte entre DTOs de la API y comandos/entidades de dominio. Sin lógica de negocio. */
public final class ConversorDto {

    private ConversorDto() {
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
        return new ProductoResponse(p.getId(), p.getCodigoUnico(), p.getNombre(),
                p.getCategoria().getId(), p.getCategoria().getNombre(),
                p.getPrecio(), p.getStock(), p.getEstado().name());
    }

    public static ClienteResponse aRespuesta(Cliente c) {
        return new ClienteResponse(c.getId(), c.getDocumentoIdentidad(), c.getNombreCompleto(),
                c.getTelefono(), c.getCorreo(), c.getEstado().name());
    }

    public static FacturaResponse aRespuesta(Factura f) {
        List<DetalleFacturaResponse> detalles = f.getDetalles().stream().map(ConversorDto::aRespuesta).toList();
        return new FacturaResponse(f.getId(), f.getNumeroFactura(), f.getFechaEmision(),
                f.getCliente().getId(), f.getCliente().getNombreCompleto(), f.getUsuarioAuthcore(),
                f.getTotal(), f.getEstadoFactura().name(), detalles);
    }

    private static DetalleFacturaResponse aRespuesta(DetalleFactura d) {
        return new DetalleFacturaResponse(d.getId(), d.getProducto().getId(),
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
