package com.pasteleria.facturacion.view.response;

import java.math.BigDecimal;

public record DetalleFacturaResponse(Long idDetalle, Long idProducto, String producto, int cantidad,
                                     BigDecimal precioUnitario, BigDecimal subtotal) {
}
