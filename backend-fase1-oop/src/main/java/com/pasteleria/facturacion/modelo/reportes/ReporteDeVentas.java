package com.pasteleria.facturacion.modelo.reportes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase abstracta (Template Method): {@link #generar} fija los pasos (filtrar → agrupar → consolidar → ordenar)
 * y cada reporte concreto decide qué incluir, cómo consolidar y en qué orden.
 */
public abstract class ReporteDeVentas<T> {

    public final List<T> generar(List<LineaVentaReporte> lineas) {
        Map<Long, List<LineaVentaReporte>> agrupadas = lineas.stream()
                .filter(this::incluir)
                .collect(Collectors.groupingBy(LineaVentaReporte::productoId));
        return agrupadas.values().stream()
                .map(this::consolidar)
                .sorted(orden())
                .toList();
    }

    protected abstract boolean incluir(LineaVentaReporte linea);

    protected abstract T consolidar(List<LineaVentaReporte> lineasDelProducto);

    protected abstract Comparator<T> orden();
}
