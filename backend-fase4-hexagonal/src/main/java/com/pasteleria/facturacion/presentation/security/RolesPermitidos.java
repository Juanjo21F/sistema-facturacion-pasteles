package com.pasteleria.facturacion.presentation.security;

import com.pasteleria.facturacion.domain.valueobject.Rol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declara qué roles (entregados por authcore-service) pueden invocar un endpoint. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RolesPermitidos {
    Rol[] value();
}
