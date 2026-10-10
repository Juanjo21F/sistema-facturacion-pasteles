package com.pasteleria.facturacion.model.service;

import com.pasteleria.facturacion.model.security.UsuarioAutenticado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Registra en el log las operaciones críticas (quién hizo qué). */
@Service
public class AuditoriaService {

    private static final Logger LOG = LoggerFactory.getLogger("AUDITORIA");

    public void registrar(UsuarioAutenticado usuario, String operacion, String detalle) {
        LOG.info("usuario={} rol={} operacion={} detalle={}", usuario.username(), usuario.rol(), operacion, detalle);
    }
}
