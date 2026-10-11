package com.pasteleria.facturacion.application.port.in;

import com.pasteleria.facturacion.domain.model.Rol;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

/** Puerto de entrada: identifica al usuario y verifica sus roles. */
public interface AutenticacionUseCase {

    UsuarioAutenticado autenticar(String token);

    void exigirRol(UsuarioAutenticado usuario, Rol... permitidos);
}
