package com.pasteleria.facturacion.infrastructure.persistence.adapter;

import com.pasteleria.facturacion.domain.entity.Categoria;
import com.pasteleria.facturacion.domain.entity.Cliente;
import com.pasteleria.facturacion.domain.entity.DetalleFactura;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.infrastructure.persistence.entity.CategoriaJpaEntity;
import com.pasteleria.facturacion.infrastructure.persistence.entity.ClienteJpaEntity;
import com.pasteleria.facturacion.infrastructure.persistence.entity.FacturaJpaEntity;
import com.pasteleria.facturacion.infrastructure.persistence.entity.ProductoJpaEntity;

import java.util.List;

/** Convierte modelo de persistencia (JPA) -> modelo de dominio. Las asociaciones deben venir cargadas. */
final class PersistenceMapper {

    private PersistenceMapper() {
    }

    static Categoria aDominio(CategoriaJpaEntity e) {
        return new Categoria(e.getId(), e.getNombre(), e.getDescripcion());
    }

    static Cliente aDominio(ClienteJpaEntity e) {
        return Cliente.reconstruir(e.getId(), e.getDocumentoIdentidad(), e.getNombreCompleto(),
                e.getTelefono(), e.getCorreo(), e.getEstado());
    }

    static Producto aDominio(ProductoJpaEntity e) {
        return Producto.reconstruir(e.getId(), e.getCodigoUnico(), e.getNombre(), aDominio(e.getCategoria()),
                e.getPrecio(), e.getStock(), e.getEstado());
    }

    static Factura aDominio(FacturaJpaEntity e) {
        List<DetalleFactura> detalles = e.getDetalles().stream()
                .map(d -> new DetalleFactura(d.getId(), aDominio(d.getProducto()), d.getCantidad(), d.getPrecioUnitario()))
                .toList();
        return Factura.reconstruir(e.getId(), e.getNumeroFactura(), e.getFechaEmision(), aDominio(e.getCliente()),
                e.getUsuarioAuthcore(), e.getEstado(), detalles);
    }
}
