package com.pasteleria.facturacion.domain.port.input;

import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;

public interface DesactivarClienteUseCase {

    void ejecutar(Long idCliente, UsuarioAutenticado usuario);
}
