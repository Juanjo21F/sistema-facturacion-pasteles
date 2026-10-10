package com.pasteleria.facturacion.model.service.command;

import java.math.BigDecimal;

public record DatosProductoCommand(String codigoUnico, String nombre, Long idCategoria,
                                   BigDecimal precio, int stock) {
}
