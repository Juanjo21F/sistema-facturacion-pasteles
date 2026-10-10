package com.pasteleria.facturacion.modelo.excepciones;

public class AuthcoreNoDisponibleException extends RuntimeException {

    public AuthcoreNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
