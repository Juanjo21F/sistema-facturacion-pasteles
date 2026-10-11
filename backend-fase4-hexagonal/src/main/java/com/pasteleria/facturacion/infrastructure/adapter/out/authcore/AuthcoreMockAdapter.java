package com.pasteleria.facturacion.infrastructure.adapter.out.authcore;

import com.pasteleria.facturacion.application.port.out.AutenticacionPort;
import com.pasteleria.facturacion.domain.exception.AutenticacionException;
import com.pasteleria.facturacion.domain.model.Rol;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SOLO PARA DESARROLLO Y PRUEBAS MANUALES: simula authcore-service con dos tokens fijos.
 * Se activa con authcore.mock.enabled=true. NUNCA debe estar activo en producción.
 */
@Component
@ConditionalOnProperty(name = "authcore.mock.enabled", havingValue = "true")
public class AuthcoreMockAdapter implements AutenticacionPort {

    private static final Logger LOG = LoggerFactory.getLogger(AuthcoreMockAdapter.class);

    public AuthcoreMockAdapter() {
        LOG.warn("authcore-service SIMULADO activo (tokens: admin-token, empleado-token). No usar en producción.");
    }

    @Override
    public UsuarioAutenticado validarToken(String token) {
        return switch (token) {
            case "admin-token" -> new UsuarioAutenticado("mock-admin-1", "admin", Rol.ADMINISTRADOR);
            case "empleado-token" -> new UsuarioAutenticado("mock-empleado-1", "empleado", Rol.EMPLEADO);
            default -> throw new AutenticacionException("Token inválido o expirado");
        };
    }
}
