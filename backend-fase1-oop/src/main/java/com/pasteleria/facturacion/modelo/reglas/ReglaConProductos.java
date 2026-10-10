package com.pasteleria.facturacion.modelo.reglas;

import com.pasteleria.facturacion.modelo.entidades.LineaVenta;
import com.pasteleria.facturacion.modelo.excepciones.ReglaDeNegocioException;

import java.util.List;

/** Clase abstracta para las reglas que revisan línea por línea (reutiliza el recorrido). */
public abstract class ReglaConProductos implements ReglaDeVenta {

    @Override
    public final void validar(ContextoVenta contexto) {
        if (contexto.lineas() == null || contexto.lineas().isEmpty()) {
            throw new ReglaDeNegocioException("La factura debe tener al menos un producto");
        }
        validarLineas(contexto.lineas());
    }

    protected abstract void validarLineas(List<LineaVenta> lineas);
}
