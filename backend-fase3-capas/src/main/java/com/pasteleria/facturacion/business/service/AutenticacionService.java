package com.pasteleria.facturacion.business.service;

import com.pasteleria.facturacion.business.exception.AccesoDenegadoException;
import com.pasteleria.facturacion.business.security.Rol;
import com.pasteleria.facturacion.business.security.UsuarioAutenticado;
import com.pasteleria.facturacion.integration.authcore.AuthcoreClient;

import org.springframework.stereotype.Service;

/** Capa de negocio: identifica al usuario y verifica sus roles. La validación del token se delega a la capa de integración. */
@Service
public class AutenticacionService {

    private final AuthcoreClient authcore;

    public AutenticacionService(AuthcoreClient authcore) {
        this.authcore = authcore;
    }

    public UsuarioAutenticado autenticar(String token) {
        return authcore.validarToken(token);
    }

    public void exigirRol(UsuarioAutenticado usuario, Rol... permitidos) {
        if (permitidos.length > 0 && !usuario.tieneAlgunRol(permitidos)) {
            throw new AccesoDenegadoException("El rol " + usuario.rol() + " no tiene permiso para esta operación");
        }
    }
}
