package com.pasteleria.facturacion.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record DetalleFacturaResponse(Long idDetalle, Long idProducto, String producto, int cantidad,
                                     BigDecimal precioUnitario, BigDecimal subtotal) {
}
