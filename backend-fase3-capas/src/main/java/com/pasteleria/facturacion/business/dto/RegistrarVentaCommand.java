package com.pasteleria.facturacion.business.dto;

import java.util.List;

/** Nótese que NO incluye total: el backend lo calcula. */
public record RegistrarVentaCommand(Long idCliente, List<ItemVentaCommand> items) {
}
