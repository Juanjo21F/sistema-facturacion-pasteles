package com.pasteleria.facturacion.application.service;

import com.pasteleria.facturacion.application.port.in.AutenticacionUseCase;
import com.pasteleria.facturacion.application.port.out.AutenticacionPort;
import com.pasteleria.facturacion.domain.exception.AccesoDenegadoException;
import com.pasteleria.facturacion.domain.model.Rol;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

public class AutenticacionService implements AutenticacionUseCase {

    private final AutenticacionPort authcore;

    public AutenticacionService(AutenticacionPort authcore) {
        this.authcore = authcore;
    }

    @Override
    public UsuarioAutenticado autenticar(String token) {
        return authcore.validarToken(token);
    }

    @Override
    public void exigirRol(UsuarioAutenticado usuario, Rol... permitidos) {
        if (permitidos.length > 0 && !usuario.tieneAlgunRol(permitidos)) {
            throw new AccesoDenegadoException("El rol " + usuario.rol() + " no tiene permiso para esta operación");
        }
    }
}
