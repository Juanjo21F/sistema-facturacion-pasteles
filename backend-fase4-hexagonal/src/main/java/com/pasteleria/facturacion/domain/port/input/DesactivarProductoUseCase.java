package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;

public interface DesactivarProductoUseCase {

    void ejecutar(Long idProducto, UsuarioAutenticado usuario);
}
