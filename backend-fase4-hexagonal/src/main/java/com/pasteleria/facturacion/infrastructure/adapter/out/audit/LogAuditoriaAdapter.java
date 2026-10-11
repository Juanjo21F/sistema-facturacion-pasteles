package com.pasteleria.facturacion.infrastructure.adapter.out.audit;

import com.pasteleria.facturacion.application.port.out.AuditoriaPort;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Adaptador de salida: registra en el log AUDITORIA las operaciones críticas (quién hizo qué). */
@Component
public class LogAuditoriaAdapter implements AuditoriaPort {

    private static final Logger LOG = LoggerFactory.getLogger("AUDITORIA");

    @Override
    public void registrar(UsuarioAutenticado usuario, String operacion, String detalle) {
        LOG.info("usuario={} rol={} operacion={} detalle={}", usuario.username(), usuario.rol(), operacion, detalle);
    }
}
