package com.pasteleria.facturacion.infrastructure.adapter.out.persistence.mapper;

import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.CategoriaEntity;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.ClienteEntity;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.DetalleFacturaEntity;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.FacturaEntity;
import com.pasteleria.facturacion.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import com.pasteleria.facturacion.domain.model.Categoria;
import com.pasteleria.facturacion.domain.model.Cliente;
import com.pasteleria.facturacion.domain.model.DetalleFactura;
import com.pasteleria.facturacion.domain.model.Factura;
import com.pasteleria.facturacion.domain.model.Producto;

import java.util.List;

/** Traduce entre entidades JPA y modelo de dominio (en ambos sentidos). */
public final class PersistenceMapper {

    private PersistenceMapper() {
    }

    // ---- entidad -> dominio ----

    public static Categoria aDominio(CategoriaEntity c) {
        return new Categoria(c.getIdCategoria(), c.getNombre());
    }

    public static Producto aDominio(ProductoEntity p) {
        return new Producto(p.getIdProducto(), p.getCodigoUnico(), p.getNombre(), aDominio(p.getCategoria()),
                p.getPrecio(), p.getStock(), p.getEstado());
    }

    public static Cliente aDominio(ClienteEntity c) {
        return new Cliente(c.getIdCliente(), c.getDocumentoIdentidad(), c.getNombreCompleto(), c.getTelefono(),
                c.getCorreo(), c.getEstado());
    }

    public static Factura aDominio(FacturaEntity f) {
        List<DetalleFactura> detalles = f.getDetalles().stream()
                .map(d -> new DetalleFactura(d.getIdDetalle(), aDominio(d.getProducto()), d.getCantidad(),
                        d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
        return new Factura(f.getIdFactura(), f.getNumeroFactura(), f.getFechaEmision(), aDominio(f.getCliente()),
                f.getUsuarioAuthcore(), f.getTotal(), f.getEstadoFactura(), detalles);
    }

    // ---- dominio -> entidad ----

    public static ProductoEntity aEntidad(Producto p, CategoriaEntity categoria) {
        ProductoEntity e = new ProductoEntity();
        e.setIdProducto(p.getId());
        e.setCodigoUnico(p.getCodigoUnico());
        e.setNombre(p.getNombre());
        e.setCategoria(categoria);
        e.setPrecio(p.getPrecio());
        e.setStock(p.getStock());
        e.setEstado(p.getEstado());
        return e;
    }

    public static ClienteEntity aEntidad(Cliente c) {
        ClienteEntity e = new ClienteEntity();
        e.setIdCliente(c.getId());
        e.setDocumentoIdentidad(c.getDocumentoIdentidad());
        e.setNombreCompleto(c.getNombreCompleto());
        e.setTelefono(c.getTelefono());
        e.setCorreo(c.getCorreo());
        e.setEstado(c.getEstado());
        return e;
    }

    public static FacturaEntity aEntidad(Factura f, ClienteEntity cliente, java.util.function.Function<Long, ProductoEntity> productoPorId) {
        FacturaEntity e = new FacturaEntity();
        e.setIdFactura(f.getId());
        e.setNumeroFactura(f.getNumeroFactura());
        e.setFechaEmision(f.getFechaEmision());
        e.setCliente(cliente);
        e.setUsuarioAuthcore(f.getUsuarioAuthcore());
        e.setTotal(f.getTotal());
        e.setEstadoFactura(f.getEstado());
        for (DetalleFactura d : f.getDetalles()) {
            DetalleFacturaEntity de = new DetalleFacturaEntity();
            de.setFactura(e);
            de.setProducto(productoPorId.apply(d.producto().getId()));
            de.setCantidad(d.cantidad());
            de.setPrecioUnitario(d.precioUnitario());
            de.setSubtotal(d.subtotal());
            e.getDetalles().add(de);
        }
        return e;
    }
}
