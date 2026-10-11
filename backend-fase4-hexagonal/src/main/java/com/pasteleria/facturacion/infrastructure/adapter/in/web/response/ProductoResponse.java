package com.pasteleria.facturacion.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record ProductoResponse(Long idProducto, String codigoUnico, String nombre, Long idCategoria,
                               String categoria, BigDecimal precio, int stock, String estado) {
}
