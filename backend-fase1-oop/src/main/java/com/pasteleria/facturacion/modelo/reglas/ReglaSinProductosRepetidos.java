package com.pasteleria.facturacion.modelo.reglas;

import com.pasteleria.facturacion.modelo.entidades.LineaVenta;
import com.pasteleria.facturacion.modelo.excepciones.ReglaDeNegocioException;

import org.springframework.core.annotation.Order;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class ReglaSinProductosRepetidos extends ReglaConProductos {

    @Override
    protected void validarLineas(List<LineaVenta> lineas) {
        Set<Long> vistos = new HashSet<>();
        for (LineaVenta linea : lineas) {
            if (!vistos.add(linea.producto().getId())) {
                throw new ReglaDeNegocioException(
                        "El producto \"" + linea.producto().getNombre() + "\" está repetido en la factura");
            }
        }
    }
}
