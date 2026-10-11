package com.pasteleria.facturacion.application.port.out;

import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

/** Puerto de salida: registra las operaciones críticas (quién hizo qué). */
public interface AuditoriaPort {

    void registrar(UsuarioAutenticado usuario, String operacion, String detalle);
}
