package com.pasteleria.facturacion.presentation.mapper;

import com.pasteleria.facturacion.business.dto.DatosClienteCommand;
import com.pasteleria.facturacion.business.dto.DatosProductoCommand;
import com.pasteleria.facturacion.business.dto.ItemVentaCommand;
import com.pasteleria.facturacion.business.dto.RegistrarVentaCommand;
import com.pasteleria.facturacion.presentation.request.ActualizarClienteRequest;
import com.pasteleria.facturacion.presentation.request.ActualizarProductoRequest;
import com.pasteleria.facturacion.presentation.request.CrearClienteRequest;
import com.pasteleria.facturacion.presentation.request.CrearFacturaRequest;
import com.pasteleria.facturacion.presentation.request.CrearProductoRequest;

import java.util.List;

/** Capa de presentación: convierte los DTOs de entrada HTTP en comandos que entiende la capa de negocio. */
public final class RequestMapper {

    private RequestMapper() {
    }

    public static DatosProductoCommand aComando(CrearProductoRequest r) {
        return new DatosProductoCommand(r.codigoUnico(), r.nombre(), r.idCategoria(), r.precio(), r.stock());
    }

    public static DatosProductoCommand aComando(ActualizarProductoRequest r) {
        return new DatosProductoCommand(r.codigoUnico(), r.nombre(), r.idCategoria(), r.precio(), r.stock());
    }

    public static DatosClienteCommand aComando(CrearClienteRequest r) {
        return new DatosClienteCommand(r.documentoIdentidad(), r.nombreCompleto(), r.telefono(), r.correo());
    }

    public static DatosClienteCommand aComando(ActualizarClienteRequest r) {
        return new DatosClienteCommand(r.documentoIdentidad(), r.nombreCompleto(), r.telefono(), r.correo());
    }

    public static RegistrarVentaCommand aComando(CrearFacturaRequest r) {
        List<ItemVentaCommand> items = r.detalles().stream()
                .map(d -> new ItemVentaCommand(d.idProducto(), d.cantidad()))
                .toList();
        return new RegistrarVentaCommand(r.idCliente(), items);
    }
}
