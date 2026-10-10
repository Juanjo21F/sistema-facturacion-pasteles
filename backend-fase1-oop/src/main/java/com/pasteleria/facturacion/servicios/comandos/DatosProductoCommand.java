package com.pasteleria.facturacion.servicios.comandos;

import java.math.BigDecimal;

public record DatosProductoCommand(String codigoUnico, String nombre, Long idCategoria,
                                   BigDecimal precio, int stock) {
}
