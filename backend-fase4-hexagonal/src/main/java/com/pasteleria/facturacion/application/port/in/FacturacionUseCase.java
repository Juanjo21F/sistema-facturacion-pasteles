package com.pasteleria.facturacion.application.port.in;

import com.pasteleria.facturacion.domain.model.Factura;
import com.pasteleria.facturacion.domain.model.UsuarioAutenticado;

/** Puerto de entrada: registrar, anular y consultar ventas. */
public interface FacturacionUseCase {

    Factura registrarVenta(RegistrarVentaCommand comando, UsuarioAutenticado usuario);

    Factura anular(Long idFactura, UsuarioAutenticado usuario);

    Factura obtenerPorId(Long idFactura);
}
