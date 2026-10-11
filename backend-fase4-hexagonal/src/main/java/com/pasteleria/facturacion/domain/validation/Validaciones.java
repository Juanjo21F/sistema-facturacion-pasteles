package com.pasteleria.facturacion.domain.validation;

import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;

import java.math.BigDecimal;

/** Validaciones reutilizables por la capa de negocio. */
public final class Validaciones {

    private Validaciones() {
    }

    public static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDeNegocioException("El campo " + campo + " es obligatorio");
        }
        return valor.trim();
    }

    public static void mayorQueCero(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new ReglaDeNegocioException("El campo " + campo + " debe ser mayor que 0");
        }
    }

    public static void cantidadPositiva(int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaDeNegocioException("La cantidad debe ser mayor que 0");
        }
    }
}
