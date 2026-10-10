package com.pasteleria.facturacion.domain.exception;

public class AuthcoreNoDisponibleException extends RuntimeException {

    public AuthcoreNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
