package com.pasteleria.facturacion.application.port.in;

import com.pasteleria.facturacion.domain.model.Producto;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

import java.util.List;

/** Puerto de entrada: casos de uso de productos. */
public interface GestionarProductosUseCase {

    Producto crear(DatosProductoCommand datos);

    Producto actualizar(Long idProducto, DatosProductoCommand datos);

    void desactivar(Long idProducto, UsuarioAutenticado usuario);

    List<Producto> listar(boolean soloDisponibles);

    Producto obtenerPorId(Long idProducto);
}
