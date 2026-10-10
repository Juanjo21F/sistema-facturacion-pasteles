package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.entity.Producto;

import java.util.List;

public interface ConsultarProductosUseCase {

    List<Producto> listar(boolean soloDisponibles);

    Producto obtenerPorId(Long idProducto);
}
