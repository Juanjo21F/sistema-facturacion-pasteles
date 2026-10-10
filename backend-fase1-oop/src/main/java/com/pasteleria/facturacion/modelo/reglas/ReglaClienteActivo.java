package com.pasteleria.facturacion.modelo.reglas;

import org.springframework.core.annotation.Order;

import org.springframework.stereotype.Component;

@Component
@Order(30)
public class ReglaClienteActivo implements ReglaDeVenta {

    @Override
    public void validar(ContextoVenta contexto) {
        contexto.cliente().validarActivo();
    }
}
