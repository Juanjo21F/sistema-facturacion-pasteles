package com.pasteleria.facturacion.application.port.in;

import java.util.List;

/** Nótese que NO incluye total: el dominio lo calcula. */
public record RegistrarVentaCommand(Long idCliente, List<ItemVentaCommand> items) {
}
