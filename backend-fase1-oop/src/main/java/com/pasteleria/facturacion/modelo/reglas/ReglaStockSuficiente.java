package com.pasteleria.facturacion.modelo.reglas;

import com.pasteleria.facturacion.modelo.base.Vendible;
import com.pasteleria.facturacion.modelo.entidades.LineaVenta;
import com.pasteleria.facturacion.modelo.excepciones.StockInsuficienteException;

import org.springframework.core.annotation.Order;

import java.util.List;
import org.springframework.stereotype.Component;

/** Verifica TODAS las líneas antes de que alguien descuente stock: así no quedan descuentos parciales. */
@Component
@Order(40)
public class ReglaStockSuficiente extends ReglaConProductos {

    @Override
    protected void validarLineas(List<LineaVenta> lineas) {
        for (LineaVenta linea : lineas) {
            Vendible vendible = linea.producto();
            if (!vendible.tieneStockPara(linea.cantidad())) {
                throw new StockInsuficienteException(linea.producto().getNombre(), linea.cantidad(), vendible.getStock());
            }
        }
    }
}
