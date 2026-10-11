package com.pasteleria.facturacion.application.port.out;

import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

/** Puerto de salida: valida tokens contra authcore-service. */
public interface AutenticacionPort {

    /**
     * Valida el token y devuelve la identidad y el rol.
     * @throws com.pasteleria.facturacion.domain.exception.AutenticacionException si el token no es válido
     */
    UsuarioAutenticado validarToken(String token);
}
