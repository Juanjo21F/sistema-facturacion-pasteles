package com.pasteleria.facturacion.view.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FacturaResponse(Long idFactura, String numeroFactura, LocalDateTime fechaEmision,
                              Long idCliente, String cliente, String usuarioAuthcore, BigDecimal total,
                              String estadoFactura, List<DetalleFacturaResponse> detalles) {
}
