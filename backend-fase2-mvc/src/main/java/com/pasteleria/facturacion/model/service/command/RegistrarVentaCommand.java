package com.pasteleria.facturacion.model.service.command;

import java.util.List;

/** Nótese que NO incluye total: el backend lo calcula. */
public record RegistrarVentaCommand(Long idCliente, List<ItemVentaCommand> items) {
}
