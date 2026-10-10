package com.pasteleria.facturacion.presentation.controller;

import com.pasteleria.facturacion.domain.port.input.AnularFacturaUseCase;
import com.pasteleria.facturacion.domain.port.input.ConsultarFacturaUseCase;
import com.pasteleria.facturacion.domain.port.input.RegistrarVentaUseCase;
import com.pasteleria.facturacion.domain.valueobject.Rol;
import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;
import com.pasteleria.facturacion.presentation.dto.ApiMapper;
import com.pasteleria.facturacion.presentation.dto.CrearFacturaRequest;
import com.pasteleria.facturacion.presentation.dto.FacturaResponse;
import com.pasteleria.facturacion.presentation.security.AutorizacionInterceptor;
import com.pasteleria.facturacion.presentation.security.RolesPermitidos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final RegistrarVentaUseCase registrarVenta;
    private final ConsultarFacturaUseCase consultarFactura;
    private final AnularFacturaUseCase anularFactura;

    public FacturaController(RegistrarVentaUseCase registrarVenta, ConsultarFacturaUseCase consultarFactura,
                             AnularFacturaUseCase anularFactura) {
        this.registrarVenta = registrarVenta;
        this.consultarFactura = consultarFactura;
        this.anularFactura = anularFactura;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse registrar(@Valid @RequestBody CrearFacturaRequest request,
                                     @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return ApiMapper.aRespuesta(registrarVenta.ejecutar(ApiMapper.aComando(request), usuario));
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.ADMINISTRADOR, Rol.EMPLEADO})
    public FacturaResponse obtener(@PathVariable Long id) {
        return ApiMapper.aRespuesta(consultarFactura.ejecutar(id));
    }

    @PutMapping("/{id}/anular")
    @RolesPermitidos(Rol.ADMINISTRADOR)
    public FacturaResponse anular(@PathVariable Long id,
                                  @RequestAttribute(AutorizacionInterceptor.ATRIBUTO_USUARIO) UsuarioAutenticado usuario) {
        return ApiMapper.aRespuesta(anularFactura.ejecutar(id, usuario));
    }
}
