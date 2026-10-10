package com.pasteleria.facturacion.business.service;

import com.pasteleria.facturacion.business.exception.ProductoInactivoException;
import com.pasteleria.facturacion.business.exception.StockInsuficienteException;
import com.pasteleria.facturacion.business.validation.Validaciones;
import com.pasteleria.facturacion.dataaccess.entity.EstadoRegistro;
import com.pasteleria.facturacion.dataaccess.entity.ProductoEntity;

import org.springframework.stereotype.Service;

/** Capa de negocio: reglas de inventario (validar, descontar y restaurar stock). */
@Service
public class InventarioService {

    public void validarDisponibleParaVenta(ProductoEntity producto) {
        if (producto.getEstado() != EstadoRegistro.ACTIVO) {
            throw new ProductoInactivoException(producto.getNombre());
        }
    }

    public void validarStock(ProductoEntity producto, int cantidad) {
        Validaciones.cantidadPositiva(cantidad);
        if (cantidad > producto.getStock()) {
            throw new StockInsuficienteException(producto.getNombre(), cantidad, producto.getStock());
        }
    }

    public void descontar(ProductoEntity producto, int cantidad) {
        validarStock(producto, cantidad);
        producto.setStock(producto.getStock() - cantidad);
    }

    public void restaurar(ProductoEntity producto, int cantidad) {
        Validaciones.cantidadPositiva(cantidad);
        producto.setStock(producto.getStock() + cantidad);
    }
}
