package com.pasteleria.facturacion.modelo.reglas;

import com.pasteleria.facturacion.modelo.entidades.LineaVenta;

import org.springframework.core.annotation.Order;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class ReglaProductosDisponibles extends ReglaConProductos {

    @Override
    protected void validarLineas(List<LineaVenta> lineas) {
        lineas.forEach(l -> l.producto().validarDisponibleParaVenta());
    }
}
