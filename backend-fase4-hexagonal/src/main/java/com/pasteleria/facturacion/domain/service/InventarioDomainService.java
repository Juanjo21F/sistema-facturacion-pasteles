package com.pasteleria.facturacion.domain.service;

import com.pasteleria.facturacion.domain.entity.DetalleFactura;
import com.pasteleria.facturacion.domain.entity.Factura;
import com.pasteleria.facturacion.domain.entity.Producto;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.domain.exception.StockInsuficienteException;
import com.pasteleria.facturacion.domain.valueobject.LineaVenta;

import java.util.List;
import java.util.Map;

/** Reglas de inventario: validar disponibilidad, descontar y restaurar stock. */
public class InventarioDomainService {

    public void validarDisponibilidad(Producto producto, int cantidad) {
        if (!producto.tieneStockPara(cantidad)) {
            throw new StockInsuficienteException(producto.getNombre(), cantidad, producto.getStock());
        }
    }

    /** Valida TODAS las líneas primero y solo después descuenta, para no dejar descuentos parciales. */
    public void descontarStock(List<LineaVenta> lineas) {
        lineas.forEach(l -> validarDisponibilidad(l.producto(), l.cantidad()));
        lineas.forEach(l -> l.producto().descontarStock(l.cantidad()));
    }

    /** Devuelve al inventario lo vendido en la factura, sobre los productos recibidos (por id). */
    public void restaurarStock(Factura factura, Map<Long, Producto> productosPorId) {
        for (DetalleFactura detalle : factura.getDetalles()) {
            Producto producto = productosPorId.get(detalle.getProducto().getIdProducto());
            if (producto == null) {
                throw new ReglaDeNegocioException("No se pudo restaurar el stock: producto no disponible");
            }
            producto.restaurarStock(detalle.getCantidad());
        }
    }
}
