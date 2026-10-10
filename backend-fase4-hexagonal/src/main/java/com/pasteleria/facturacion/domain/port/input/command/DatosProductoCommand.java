package com.pasteleria.facturacion.domain.port.input.command;

import java.math.BigDecimal;

public record DatosProductoCommand(String codigoUnico, String nombre, Long idCategoria,
                                   BigDecimal precio, int stock) {
}
