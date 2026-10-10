package com.pasteleria.facturacion.servicios;

import com.pasteleria.facturacion.modelo.base.EntidadActivable;
import com.pasteleria.facturacion.seguridad.UsuarioAutenticado;

import org.springframework.transaction.annotation.Transactional;

/** Segundo nivel: servicios de entidades activables. Aquí vive, una sola vez, "desactivar + auditar". */
public abstract class ServicioActivable<T extends EntidadActivable> extends ServicioBase<T> {

    private final ServicioAuditoria auditoria;

    protected ServicioActivable(ServicioAuditoria auditoria) {
        this.auditoria = auditoria;
    }

    protected abstract String operacionDesactivar();

    protected abstract String detalleAuditoria(T entidad);

    @Transactional
    public void desactivar(Long id, UsuarioAutenticado usuario) {
        T entidad = obtenerPorId(id);
        entidad.desactivar();
        guardar(entidad);
        auditoria.registrar(usuario, operacionDesactivar(), detalleAuditoria(entidad));
    }
}
