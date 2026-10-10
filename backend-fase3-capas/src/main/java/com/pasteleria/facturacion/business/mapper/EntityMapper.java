package com.pasteleria.facturacion.business.mapper;

import com.pasteleria.facturacion.business.dto.ClienteResponse;
import com.pasteleria.facturacion.business.dto.DetalleFacturaResponse;
import com.pasteleria.facturacion.business.dto.FacturaResponse;
import com.pasteleria.facturacion.business.dto.ProductoResponse;
import com.pasteleria.facturacion.business.dto.ReporteMensualResponse;
import com.pasteleria.facturacion.business.dto.ReporteProductoResponse;
import com.pasteleria.facturacion.business.dto.ReporteProductoVendido;
import com.pasteleria.facturacion.dataaccess.entity.ClienteEntity;
import com.pasteleria.facturacion.dataaccess.entity.FacturaEntity;
import com.pasteleria.facturacion.dataaccess.entity.ProductoEntity;

import java.util.List;

/** Capa de negocio: convierte entidades de datos en DTOs de salida, para que la presentación nunca vea entidades. */
public final class EntityMapper {

    private EntityMapper() {
    }

    public static ProductoResponse aRespuesta(ProductoEntity p) {
        return new ProductoResponse(p.getIdProducto(), p.getCodigoUnico(), p.getNombre(),
                p.getCategoria().getIdCategoria(), p.getCategoria().getNombre(),
                p.getPrecio(), p.getStock(), p.getEstado().name());
    }

    public static ClienteResponse aRespuesta(ClienteEntity c) {
        return new ClienteResponse(c.getIdCliente(), c.getDocumentoIdentidad(), c.getNombreCompleto(),
                c.getTelefono(), c.getCorreo(), c.getEstado().name());
    }

    public static FacturaResponse aRespuesta(FacturaEntity f) {
        List<DetalleFacturaResponse> detalles = f.getDetalles().stream()
                .map(d -> new DetalleFacturaResponse(d.getIdDetalle(), d.getProducto().getIdProducto(),
                        d.getProducto().getNombre(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
        return new FacturaResponse(f.getIdFactura(), f.getNumeroFactura(), f.getFechaEmision(),
                f.getCliente().getIdCliente(), f.getCliente().getNombreCompleto(), f.getUsuarioAuthcore(),
                f.getTotal(), f.getEstadoFactura().name(), detalles);
    }

    public static ReporteMensualResponse aRespuesta(int mes, int anio, List<ReporteProductoVendido> filas) {
        List<ReporteProductoResponse> productos = filas.stream()
                .map(f -> new ReporteProductoResponse(f.productoId(), f.codigoProducto(), f.nombreProducto(),
                        f.cantidadVendida(), f.totalIngresos()))
                .toList();
        return new ReporteMensualResponse(mes, anio, productos);
    }
}
