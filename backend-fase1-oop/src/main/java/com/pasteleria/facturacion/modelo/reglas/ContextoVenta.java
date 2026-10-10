package com.pasteleria.facturacion.modelo.reglas;

import com.pasteleria.facturacion.modelo.entidades.Cliente;
import com.pasteleria.facturacion.modelo.entidades.LineaVenta;

import java.util.List;

/** Datos sobre los que se evalúan las reglas de una venta. */
public record ContextoVenta(Cliente cliente, List<LineaVenta> lineas) {
}
